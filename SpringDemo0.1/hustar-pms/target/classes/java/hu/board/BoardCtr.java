package hu.board;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import hu.admin.board.BoardGroupSvc;
import hu.admin.board.BoardGroupVO;
import hu.common.Field3VO;
import hu.common.UtilEtc;
import hu.etc.EtcSvc;

@Controller
public class BoardCtr {
	@Autowired
	private BoardSvc boardSvc;
	
	@Autowired
	private BoardGroupSvc boardGroupSvc;
	
	@Autowired
	private EtcSvc etcSvc;
	
	static final Logger LOGGER = LoggerFactory.getLogger(BoardCtr.class);
	
    /**
     * 게시판 리스트.
     */
	@RequestMapping(value = "/boardList")
	public String boardList(HttpServletRequest request, BoardSearchVO searchVO, ModelMap modelMap) {
		System.out.println("===========> boardlist1");
		String globalKeyword = request.getParameter("globalKeyword"); // jsp 왼쪽 사이드 바으로부터 찾기
		System.out.println("===========> boardlist2");
		if(globalKeyword!=null & !"".equals(globalKeyword)) {
			searchVO.setSearchKeyword(globalKeyword);
		}
		System.out.println("===========> boardlist3");
		
		String userno = request.getSession().getAttribute("userno").toString();
		
		Integer alertcount = etcSvc.selectAlertCount(userno);
		modelMap.addAttribute("alertcount", alertcount);
		
		if(searchVO.getBgno() != null && !"".equals(searchVO.getBgno())) {
			BoardGroupVO bgInfo = boardSvc.selectBoardGroupOne4Used(searchVO.getBgno());
			if(bgInfo == null) {
				return "board/BoardGroupFail";
			}
			modelMap.addAttribute("bgInfo", bgInfo);
		}
		
		List<?> noticelist = boardSvc.selectNoticeList(searchVO);
		
		searchVO.pageCalculate(boardSvc.selectBoardCount(searchVO)); //startRow, endRow
		List<?> listview = boardSvc.selectBoardList(searchVO);
		
		modelMap.addAttribute("searchVO", searchVO);
		modelMap.addAttribute("listview", listview);
		modelMap.addAttribute("noticelist", noticelist);
		
		if(searchVO.getBgno() == null || "".equals(searchVO.getBgno())) {
			return "board/BoardListAll"; //?????
		}
		
		return "board/BoardList";
	}
	
	/**
	 * 글 읽기
	 */
	@RequestMapping(value = "/boardRead")
	public String boardRead(HttpServletRequest request, ModelMap modelMap) {
		String userno = request.getSession().getAttribute("userno").toString();
		
		Integer alertcount = etcSvc.selectAlertCount(userno);
		modelMap.addAttribute("alertcount", alertcount);
		
		String bgno = request.getParameter("bgno");
		String brdno = request.getParameter("brdno");
		
		Field3VO f3 = new Field3VO(brdno, userno, null);
		
		boardSvc.updateBoardRead(f3);
		BoardVO boardInfo = boardSvc.selectBoardOne(f3);
		List<?> listview = boardSvc.selectBoardFileList(brdno);
		List<?> replylist = boardSvc.selectBoardReplyList(brdno);
		
		BoardGroupVO bgInfo = boardSvc.selectBoardGroupOne4Used(boardInfo.getBgno());
		if(bgInfo==null) {
			return "board/BoardGroupFail";
		}
		
		modelMap.addAttribute("boardInfo", boardInfo);
        modelMap.addAttribute("listview", listview);
        modelMap.addAttribute("replylist", replylist);
        modelMap.addAttribute("bgno", bgno);
        modelMap.addAttribute("bgInfo", bgInfo);
        
		return "board/BoardRead";
	}
	
	
	/**
	 * 댓글 저장
	 */
	@RequestMapping(value = "/boardReplySave")
    public String boardReplySave(HttpServletRequest request, HttpServletResponse response, BoardReplyVO boardReplyInfo, ModelMap modelMap) {
        String userno = request.getSession().getAttribute("userno").toString();
        boardReplyInfo.setUserno(userno);

        if (boardReplyInfo.getReno() != null && !"".equals(boardReplyInfo.getReno())) {    // check auth for update
            String chk = boardSvc.selectBoardReplyAuthChk(boardReplyInfo);
            if (chk == null) {
                UtilEtc.responseJsonValue(response, "");
                return null;
            }
        }

        boardReplyInfo = boardSvc.insertBoardReply(boardReplyInfo);
        //boardReplyInfo.setRewriter(request.getSession().getAttribute("usernm").toString());

        modelMap.addAttribute("replyInfo", boardReplyInfo);
        
        return "board/BoardReadAjax4Reply";        
    }
	
}
