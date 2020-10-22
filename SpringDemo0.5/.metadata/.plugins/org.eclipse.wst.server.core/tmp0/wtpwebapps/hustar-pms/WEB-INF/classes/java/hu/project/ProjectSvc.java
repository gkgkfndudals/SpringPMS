package hu.project;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.common.SearchVO;

@Service
public class ProjectSvc {

	@Autowired
	private SqlSessionTemplate sqlSession;
	
	static final Logger LOGGER = LoggerFactory.getLogger(ProjectSvc.class);
	
	/**
	 *  프로젝트 리스트
	 */
	public Integer selectProjectCount(SearchVO param) {
		return sqlSession.selectOne("selectProjectCount", param);
	}
	
	public List<?> selectProjectList(SearchVO param) {
		return sqlSession.selectList("selectProjectList", param);
	}
}
