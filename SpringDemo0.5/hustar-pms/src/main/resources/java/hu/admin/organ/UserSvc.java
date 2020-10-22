package hu.admin.organ;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.member.UserVO;

@Service
public class UserSvc {
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	public List<?> selectUserList(String param) {
		return sqlSession.selectList("selectUserList", param);
	}
	
	public String selectUserID(String param) {
        return sqlSession.selectOne("selectUserID", param);
    }
	
	/**
     * 사용자 저장.     
     */
    public void insertUser(UserVO param) {
        if (param.getUserno() == null || "".equals(param.getUserno())) {
             sqlSession.insert("insertUser", param);
        } else {
            sqlSession.insert("updateUser", param);
        }
    }
	
    /**
     * 사용자 삭제
     */
    public void deleteUser(String param) {
        sqlSession.delete("deleteUser", param);
    }
    
    /**
     * 사용자 조회
     */
    public UserVO selectUserOne(String param) {
        return sqlSession.selectOne("selectUserOne", param);
    }
    
    
    /**
     * 로그인 한 사용자 정보 수정 
     */
    public void updateUserByMe(UserVO param) {
        sqlSession.delete("updateUserByMe", param); //update문에서 delete 사용한 이유
        											//성공시 삭제 된 row의 수를 보기 위해 -> 수정 성공된 row의 수를 봐서 log를 찍어 확인하기 위해서
    }
    
    public void updateUserPassword(UserVO param) {
        sqlSession.delete("updateUserPassword", param);
    }
}
