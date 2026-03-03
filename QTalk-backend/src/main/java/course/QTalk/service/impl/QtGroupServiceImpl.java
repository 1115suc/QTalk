package course.QTalk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.constant.CommonConstant;
import course.QTalk.constant.MinIOConstant;
import course.QTalk.exception.QTException;
import course.QTalk.exception.QTWebException;
import course.QTalk.mapper.QtGroupMemberMapper;
import course.QTalk.minio.constant.MinioErrorConstant;
import course.QTalk.minio.exception.MinioException;
import course.QTalk.minio.model.FileUploadResponse;
import course.QTalk.pojo.bo.GroupMemberInfoBO;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.GroupRole;
import course.QTalk.pojo.enums.GroupStatus;
import course.QTalk.pojo.enums.LoginTypeEnum;
import course.QTalk.pojo.po.QtGroup;
import course.QTalk.pojo.po.QtGroupMember;
import course.QTalk.pojo.vo.request.CreatGroupVO;
import course.QTalk.pojo.vo.request.GroupBasicInfoVO;
import course.QTalk.pojo.vo.request.UpdateGroupInfoVO;
import course.QTalk.pojo.vo.response.GroupDetailInfoVO;
import course.QTalk.pojo.vo.response.GroupInfoVO;
import course.QTalk.pojo.vo.response.MyGroupVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.service.QtGroupService;
import course.QTalk.mapper.QtGroupMapper;
import course.QTalk.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import course.QTalk.minio.service.MinIOFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 32147
 * @description 针对表【qt_group(QT群组信息表)】的数据库操作Service实现
 * @createDate 2026-02-28 11:32:12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QtGroupServiceImpl extends ServiceImpl<QtGroupMapper, QtGroup>
        implements QtGroupService {

    private final static String separator = "/";

    private final RedisUtil redisUtil;
    private final MinIOFileService minIOFileService;
    private final QtGroupMapper qtGroupMapper;
    private final QtGroupMemberMapper qtGroupMemberMapper;

    private TokenUserDTO getTokenUserDTO(String token, Integer type) {
        String loginPrefix = LoginTypeEnum.of(type).getPrefix();
        String tokenLoginInfo = (String) redisUtil.get(loginPrefix + token);
        TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
        return tokenUserDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createGroup(String token, Integer type, CreatGroupVO creatGroupVO) {
        TokenUserDTO tokenUserDTO = getTokenUserDTO(token, type);

        String groupId = "Q" + RandomUtil.randomNumbers(9);
        String imgPath = null;
        if (ObjectUtil.isNotEmpty(creatGroupVO.getAvatar().getOriginalFilename())) {
            try {
                imgPath = MinIOConstant.GROUP_AVATAR_DIR + groupId + separator;
                MultipartFile avatar = creatGroupVO.getAvatar();
                FileUploadResponse fileUploadResponse = minIOFileService.uploadImage(avatar, MinIOConstant.BUCKET_NAME, imgPath, false);
                // 群头像路径
                imgPath = fileUploadResponse.getFileName();
            } catch (MinioException e) {
                log.error(MinioErrorConstant.ERROR_1008_MINIO_FILE_ALREADY_EXISTS);
                throw new QTWebException(e.getMessage(), ResponseCode.ERROR.getCode());
            } catch (IllegalArgumentException e) {
                log.error("创建群聊失败，群聊头像上传失败", e);
                throw new QTWebException(ResponseCode.GROUP_AVATAR_UPLOAD_ERROR.getMessage(), ResponseCode.GROUP_AVATAR_UPLOAD_ERROR.getCode());
            } catch (Exception e) {
                log.error("创建群聊失败，未知错误", e);
                throw new QTException(e.getMessage(), ResponseCode.ERROR.getCode());
            }
        }

        QtGroup qtGroup = QtGroup.builder()
                .groupId(groupId)
                .name(creatGroupVO.getGroupName())
                .avatar(imgPath)
                .ownerUid(tokenUserDTO.getUid())
                .notice(creatGroupVO.getNotice())
                .currentCount(CommonConstant.ONE)
                .allowInvite(creatGroupVO.getAllowInvite())
                .joinType(creatGroupVO.getJoinType())
                .status(GroupStatus.NORMAL.getCode())
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        qtGroupMapper.insert(qtGroup);

        QtGroupMember qtGroupMember = QtGroupMember.builder()
                .groupId(groupId)
                .userUid(tokenUserDTO.getUid())
                .role(GroupRole.OWNER.getCode())
                .alias(tokenUserDTO.getNickname())
                .isTop(CommonConstant.ZERO)
                .isDisturb(CommonConstant.ZERO)
                .joinType(CommonConstant.ZERO)
                .isQuit(CommonConstant.ZERO)
                .joinTime(new Date())
                .build();
        qtGroupMemberMapper.insert(qtGroupMember);

        // TODO 创建会话

        // TODO 发送消息

        return R.ok(ResponseCode.GROUP_CREATE_SUCCESS.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateGroupInfo(String token, Integer type, UpdateGroupInfoVO updateGroupInfoVO) {
        TokenUserDTO tokenUserDTO = getTokenUserDTO(token, type);
        String uid = tokenUserDTO.getUid();
        String groupId = updateGroupInfoVO.getGroupId();

        // 校验是否存在群聊
        LambdaQueryWrapper<QtGroup> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(QtGroup::getGroupId, groupId);
        QtGroup qtGroup = qtGroupMapper.selectOne(groupQuery);

        if (ObjectUtil.isNull(qtGroup) || qtGroup.getStatus().equals(GroupStatus.DISMISSED.getCode())) {
            throw new QTWebException(ResponseCode.GROUP_NOT_EXISTS.getMessage(), ResponseCode.GROUP_NOT_EXISTS.getCode());
        }

        // 校验用户是否在群聊中
        LambdaQueryWrapper<QtGroupMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(QtGroupMember::getGroupId, groupId);
        memberQuery.eq(QtGroupMember::getUserUid, uid);
        QtGroupMember qtGroupMember = qtGroupMemberMapper.selectOne(memberQuery);

        if (ObjectUtil.isNull(qtGroupMember) || qtGroupMember.getIsQuit().equals(CommonConstant.ONE)) {
            throw new QTWebException(ResponseCode.USER_NOT_IN_GROUP.getMessage(), ResponseCode.USER_NOT_IN_GROUP.getCode());
        }

        // 校验权限（群主或管理员）
        if (!qtGroupMember.getRole().equals(GroupRole.OWNER.getCode()) && !qtGroupMember.getRole().equals(GroupRole.ADMIN.getCode())) {
            throw new QTWebException(ResponseCode.NOT_PERMISSION.getMessage(), ResponseCode.NOT_PERMISSION.getCode());
        }

        // 更新群组信息
        boolean updated = false;
        if (StrUtil.isNotBlank(updateGroupInfoVO.getName())) {
            qtGroup.setName(updateGroupInfoVO.getName());
            updated = true;
        }
        if (ObjectUtil.isNotEmpty(updateGroupInfoVO.getAvatar().getOriginalFilename())) {
            try {
                String imgPath = MinIOConstant.GROUP_AVATAR_DIR + groupId + separator;
                MultipartFile avatar = updateGroupInfoVO.getAvatar();
                FileUploadResponse fileUploadResponse = minIOFileService.uploadImage(avatar, MinIOConstant.BUCKET_NAME, imgPath, false);
                // 群头像路径
                imgPath = fileUploadResponse.getFileName();
                qtGroup.setAvatar(imgPath);
            } catch (MinioException e) {
                log.error(MinioErrorConstant.ERROR_1008_MINIO_FILE_ALREADY_EXISTS);
                throw new QTWebException(e.getMessage(), ResponseCode.ERROR.getCode());
            } catch (IllegalArgumentException e) {
                log.error("群聊信息更新失败，原因群聊头像上传失败", e);
                throw new QTWebException(ResponseCode.GROUP_AVATAR_UPLOAD_ERROR.getMessage(), ResponseCode.GROUP_AVATAR_UPLOAD_ERROR.getCode());
            } catch (Exception e) {
                log.error("创建群聊失败，未知错误", e);
                throw new QTException(e.getMessage(), ResponseCode.ERROR.getCode());
            }
        }
        if (StrUtil.isNotBlank(updateGroupInfoVO.getNotice())) {
            qtGroup.setNotice(updateGroupInfoVO.getNotice());
            updated = true;
        }
        if (ObjectUtil.isNotNull(updateGroupInfoVO.getJoinType())) {
            qtGroup.setJoinType(updateGroupInfoVO.getJoinType());
            updated = true;
        }

        if (updated) {
            qtGroup.setUpdateTime(new Date());
            qtGroupMapper.updateById(qtGroup);
        }

        return R.ok(ResponseCode.GROUP_UPDATE_SUCCESS);
    }

    @Override
    public R<List<MyGroupVO>> queryMyGroups(String token, Integer type) {
        TokenUserDTO tokenUserDTO = getTokenUserDTO(token, type);

        String uid = tokenUserDTO.getUid();
        LambdaQueryWrapper<QtGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QtGroupMember::getUserUid, uid);
        queryWrapper.eq(QtGroupMember::getIsQuit, CommonConstant.ZERO);
        queryWrapper.orderByDesc(QtGroupMember::getRole);
        List<QtGroupMember> qtGroupMembers = qtGroupMemberMapper.selectList(queryWrapper);

        if (CollectionUtil.isNotEmpty(qtGroupMembers)) {
            List<MyGroupVO> myGroupVOList = qtGroupMembers.stream().map(qtGroupMember -> {
                LambdaQueryWrapper<QtGroup> groupQuery = new LambdaQueryWrapper<>();
                groupQuery.eq(QtGroup::getGroupId, qtGroupMember.getGroupId());
                QtGroup qtGroup = qtGroupMapper.selectOne(groupQuery);

                MyGroupVO myGroupVO = new MyGroupVO();
                myGroupVO.setGroupId(qtGroup.getGroupId());
                myGroupVO.setGroupName(qtGroup.getName());
                myGroupVO.setGroupAvatar(qtGroup.getAvatar());
                myGroupVO.setRole(qtGroupMember.getRole());
                return myGroupVO;
            }).collect(Collectors.toList());

            return R.ok(myGroupVOList);
        }

        return R.ok(ResponseCode.GROUP_LIST_EMPTY.getMessage());
    }

    @Override
    public R<GroupInfoVO> queryGroupInfo(GroupBasicInfoVO groupBasicInfoVO) {
        if(StrUtil.isBlank(groupBasicInfoVO.getGroupId()) && StrUtil.isBlank(groupBasicInfoVO.getName())) {
            throw new QTWebException(ResponseCode.GROUP_ID_OR_NAME_EMPTY.getMessage(), ResponseCode.GROUP_ID_OR_NAME_EMPTY.getCode());
        }

        LambdaQueryWrapper<QtGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(groupBasicInfoVO.getGroupId()),QtGroup::getGroupId, groupBasicInfoVO.getGroupId());
        queryWrapper.like(StrUtil.isNotBlank(groupBasicInfoVO.getName()),QtGroup::getName, groupBasicInfoVO.getName());
        // TODO 拓展成分页查询群（待修复）
        QtGroup qtGroup = qtGroupMapper.selectList(queryWrapper).get(0);

        if (ObjectUtil.isNotNull(qtGroup)) {
            GroupInfoVO groupInfoVO = new GroupInfoVO();
            BeanUtil.copyProperties(qtGroup, groupInfoVO);

            return R.ok(groupInfoVO);
        }

        return R.error(ResponseCode.GROUP_NOT_EXISTS.getCode(), ResponseCode.GROUP_NOT_EXISTS.getMessage());
    }

    @Override
    public R<GroupDetailInfoVO> getGroupDetailInfo(String token, Integer type, String groupId) {
        // 校验是否存在群聊
        LambdaQueryWrapper<QtGroup> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(QtGroup::getGroupId, groupId);
        QtGroup qtGroup = qtGroupMapper.selectOne(groupQuery);

        // 不存在该群聊或者群聊已解散
        if (ObjectUtil.isNull(qtGroup) || qtGroup.getStatus().equals(GroupStatus.DISMISSED.getCode())) {
            return R.error(ResponseCode.GROUP_NOT_EXISTS.getCode(), ResponseCode.GROUP_NOT_EXISTS.getMessage());
        }

        TokenUserDTO tokenUserDTO = getTokenUserDTO(token, type);

        // 校验用户是否在群聊中
        String userId = tokenUserDTO.getUid();
        LambdaQueryWrapper<QtGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QtGroupMember::getUserUid, userId);
        queryWrapper.eq(QtGroupMember::getGroupId, groupId);
        QtGroupMember qtGroupMember = qtGroupMemberMapper.selectOne(queryWrapper);

        // 用户已经推出群聊不在群组中,或者不存在该群聊
        if (ObjectUtil.isNull(qtGroupMember) || qtGroupMember.getIsQuit().equals(CommonConstant.ONE)) {
            return R.error(ResponseCode.USER_NOT_IN_GROUP.getCode(), ResponseCode.USER_NOT_IN_GROUP.getMessage());
        }

        // 获取群成员信息
        // TODO 获取群成员信息测试
        List<GroupMemberInfoBO> qtGroupMembers = qtGroupMemberMapper.selectGroupMembersInfo(groupId);

        // 群成员为空，系统查询错误
        if (CollectionUtil.isEmpty(qtGroupMembers)) {
            log.error("群成员为空, 群id为{}", groupId);
            throw new QTWebException(ResponseCode.ERROR.getMessage(), ResponseCode.ERROR.getCode());
        }

        GroupDetailInfoVO groupDetailInfoVO = GroupDetailInfoVO.builder()
                .groupId(qtGroup.getGroupId())
                .name(qtGroup.getName())
                .avatar(qtGroup.getAvatar())
                .ownerUid(qtGroup.getOwnerUid())
                .notice(qtGroup.getNotice())
                .currentCount(qtGroup.getCurrentCount())
                .allowInvite(qtGroup.getAllowInvite())
                .createTime(qtGroup.getCreateTime())
                .members(qtGroupMembers)
                .build();

        return R.ok(groupDetailInfoVO);
    }

}




