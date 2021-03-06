package hu.member;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberSvc {

	@Autowired
	private SqlSessionTemplate sqlSession;
	
	public UserVO selectMember4Login(LoginVO param) {
		return sqlSession.selectOne("selectMember4Login", param);
	}
	
	public void insertLogIn(String param) {
		sqlSession.insert("insertLogIn", param);
	}
}
