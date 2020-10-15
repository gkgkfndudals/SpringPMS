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
}
