package course.QTalk.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.LoginTypeEnum;
import course.QTalk.pojo.po.SysVersion;
import course.QTalk.pojo.vo.request.UploadVersionVO;
import course.QTalk.pojo.vo.response.LoadVersionVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.service.SysVersionService;
import course.QTalk.mapper.SysVersionMapper;
import course.QTalk.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 32147
* @description 针对表【sys_version(系统版本管理表)】的数据库操作Service实现
* @createDate 2026-03-04 15:48:07
*/
@Service
@RequiredArgsConstructor
public class SysVersionServiceImpl extends ServiceImpl<SysVersionMapper, SysVersion>
    implements SysVersionService{

    private final RedisUtil redisUtil;
    private final SysVersionMapper sysVersionMapper;

    private TokenUserDTO getTokenUserDTO(String token, Integer type) {
        String redisPrefix = LoginTypeEnum.of(type).getPrefix();
        Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
        TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
        return tokenUserDTO;
    }

    @Override
    public R<List<LoadVersionVO>> loadVersion(String token, Integer type) {
        TokenUserDTO tokenUserDTO = getTokenUserDTO(token, type);

        List<SysVersion> sysVersions = sysVersionMapper.selectList(new LambdaQueryWrapper<SysVersion>()
                .orderByDesc(SysVersion::getPublishTime));
        List<LoadVersionVO> loadVersionVOS = sysVersions.stream().map(sysVersion -> {
            LoadVersionVO loadVersionVO = new LoadVersionVO();
            BeanUtil.copyProperties(sysVersion, loadVersionVO);
            return loadVersionVO;
        }).collect(Collectors.toList());

        return R.ok(loadVersionVOS);
    }

    @Override
    public R<String> uploadVersion(String token, Integer type, UploadVersionVO uploadVersionVO, MultipartFile file) {
        // TODO 版本文件上传待完善
        return null;
    }
}




