package course.QTalk.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
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
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.GroupRole;
import course.QTalk.pojo.enums.GroupStatus;
import course.QTalk.pojo.enums.LoginTypeEnum;
import course.QTalk.pojo.po.QtGroup;
import course.QTalk.pojo.po.QtGroupMember;
import course.QTalk.pojo.vo.request.CreatGroupVO;
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
    public R<List<MyGroupVO>> queryMyGroups(String token, Integer type) {
        TokenUserDTO tokenUserDTO = getTokenUserDTO(token, type);

        String uid = tokenUserDTO.getUid();
        LambdaQueryWrapper<QtGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QtGroupMember::getUserUid, uid);
        queryWrapper.eq(QtGroupMember::getIsQuit, CommonConstant.ZERO);
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
}




