package hu.board;

import java.io.IOException;
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
import hu.common.FileUtil;
import hu.common.FileVO;
import hu.common.TreeMaker;
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
		String globalKeyword = request.getParameter("globalKeyword"); // jsp 왼쪽 사이드 바 검색필터에서 찾기 -> default 제목, 내용
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
     * 글 쓰기. 
     */
    @RequestMapping(value = "/boardForm")
    public String boardForm(HttpServletRequest request, ModelMap modelMap) {
    	System.out.println("================= boardForm으로 들어왔다 ================ " );
        String userno = request.getSession().getAttribute("userno").toString();
        
        Integer alertcount = etcSvc.selectAlertCount(userno);
        modelMap.addAttribute("alertcount", alertcount);
        
        String bgno = request.getParameter("bgno");
        String brdno = request.getParameter("brdno"); //글쓰기 할 때 null로 잡힘
        
        System.out.println("=================> " + bgno );
        System.out.println("=================> " + brdno );
        
        if (brdno != null) {
        	System.out.println("========= brdno!=null ========> ");
            BoardVO boardInfo = boardSvc.selectBoardOne(new Field3VO(brdno, null, null));
            List<?> listview = boardSvc.selectBoardFileList(brdno);
        
            modelMap.addAttribute("boardInfo", boardInfo);
            modelMap.addAttribute("listview", listview);
            bgno = boardInfo.getBgno();
        }
        BoardGroupVO bgInfo = boardSvc.selectBoardGroupOne4Used(bgno);
        if (bgInfo == null) {
            return "board/BoardGroupFail";
        }
        
        modelMap.addAttribute("bgno", bgno);
        modelMap.addAttribute("bgInfo", bgInfo);
        
        return "board/BoardForm";
    }
	
	/**
	 * 글 저장.
	 */
	
	@RequestMapping(value = "/boardSave")
	public String boardSave(HttpServletRequest request, BoardVO boardInfo) {
		String userno = request.getSession().getAttribute("userno").toString();
		boardInfo.setUserno(userno);
		
		if(boardInfo.getBrdno() != null && !"".equals(boardInfo.getBrdno())) {
			String chk = boardSvc.selectBoardAuthChk(boardInfo);
			if(chk == null) {
				return "common/noAuth";
			}
		}
		
		String[] fileno = request.getParameterValues("fileno");  //수정 시 삭제하고 싶은 선택된 파일들
		FileUtil fs = new FileUtil();
		List<FileVO> filelist = fs.saveAllFiles(boardInfo.getUploadfile());
		
		boardSvc.insertBoard(boardInfo, filelist, fileno);
		
		return "redirect:/boardList?bgno=" + boardInfo.getBgno();
	}
	
	
	/**
     * 글 삭제.
     */
    @RequestMapping(value = "/boardDelete")
    public String boardDelete(HttpServletRequest request) {
    	String brdno = request.getParameter("brdno");
    	String bgno = request.getParameter("bgno");
    	String userno = request.getSession().getAttribute("userno").toString();
    	
    	BoardVO boardInfo = new BoardVO();
    	boardInfo.setBrdno(brdno);
    	boardInfo.setUserno(userno);
    	String chk = boardSvc.selectBoardAuthChk(boardInfo); //삭제 프래그가 N, 선택된 brdno, userno인 
    														//게시판번호 select 
    	
    	if(chk==null) {
    		return "common/noAuth";
    	}
    	
    	boardSvc.deleteBoardOne(brdno);
    	
        return "redirect:/boardList?bgno=" + bgno;
    }
    
    /*=============================================================================*/
    /**
     * 게시판 트리. Ajax용.      //BoardList.jsp 에서 showBoardList() 함수를 사용 (jquery)  url: "boardListByAjax",
																					type:"post", 
																					dataType: "json",
     */
    @RequestMapping(value = "/boardListByAjax")
       public void boardListByAjax(HttpServletResponse response, ModelMap modelMap) {
        List<?> listview   = boardGroupSvc.selectBoardGroupList();

        TreeMaker tm = new TreeMaker();
        String treeStr = tm.makeTreeByHierarchy(listview);
        
        System.out.println("==============================> " + treeStr);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().print(treeStr);
        } catch (IOException ex) {
            LOGGER.error("boardListByAjax");
        }
        
    }
	/*================================================*/
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
	
	/**
     * 댓글 삭제.
     */
    @RequestMapping(value = "/boardReplyDelete")
    public void boardReplyDelete(HttpServletRequest request, HttpServletResponse response, BoardReplyVO boardReplyInfo) {
        String userno = request.getSession().getAttribute("userno").toString();
        boardReplyInfo.setUserno(userno);

        if (boardReplyInfo.getReno() != null && !"".equals(boardReplyInfo.getReno())) {    // check auth for update
            String chk = boardSvc.selectBoardReplyAuthChk(boardReplyInfo);
            if (chk == null) {
                UtilEtc.responseJsonValue(response, "FailAuth");
                return;
            }
        }
        
        if (!boardSvc.deleteBoardReply(boardReplyInfo.getReno()) ) {
            UtilEtc.responseJsonValue(response, "Fail");
        } else {
            UtilEtc.responseJsonValue(response, "OK");
        }
    }
	
	/*====================================================*/
	 /**
     * 좋아요 저장.     
     */
    @RequestMapping(value = "/addBoardLike")
    public void addBoardLike(HttpServletRequest request, HttpServletResponse response) {
        String brdno = request.getParameter("brdno");
        String userno = request.getSession().getAttribute("userno").toString();

        boardSvc.insertBoardLike( new Field3VO(brdno, userno, null) );

        UtilEtc.responseJsonValue(response, "OK");
    }
	
}
