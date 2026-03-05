package course.QTalk.service;

import course.QTalk.pojo.vo.request.EmailCheckCodeVO;
import course.QTalk.pojo.vo.response.R;

public interface EmailCodeService {

	R sendEmailCode(EmailCheckCodeVO email);

	void checkCode(String email,String code);
}