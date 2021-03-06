package hu.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import hu.common.DateVO;
import hu.common.Util4calen;
import hu.etc.EtcSvc;

@Controller
public class IndexCtr {
	@Autowired
	private EtcSvc etcSvc;

	@Autowired
	private IndexSvc indexSvc;

	///////// 메인 페이지/////////////////
	@RequestMapping(value = "/index")
	public String index(HttpServletRequest request, ModelMap modelMap) {
		String userno = request.getSession().getAttribute("userno").toString();

		Date today = Util4calen.getToday();
		calCalen(today, modelMap);

		Integer alertcount = etcSvc.selectAlertCount(userno);
		modelMap.addAttribute("alertcount", alertcount);

		List<?> listview = indexSvc.selectRecentNews();
		List<?> noticeList = indexSvc.selectNoticeListTop5();
		List<?> listtime = indexSvc.selectTimeLine();

		modelMap.addAttribute("listview", listview);
		modelMap.addAttribute("noticeList", noticeList);
		modelMap.addAttribute("listtime", listtime);

		return "main/index";
	}

	/**
     * week calendar in main page. 
     * Ajax.
     */
    @RequestMapping(value = "/moveDate")
    public String moveDate(HttpServletRequest request, ModelMap modelMap) {
        String date = request.getParameter("date");

        Date today = Util4calen.getToday(date);
        
        calCalen(today, modelMap);
        
        return "main/indexCalen";
    }
    
	private String calCalen(Date targetDay, ModelMap modelMap) {
		List<DateVO> calenList = new ArrayList<DateVO>();

		Date today = Util4calen.getToday(); 					// 시스템의 오늘 일자
		int month = Util4calen.getMonth(targetDay);				// 월 추출
		int week = Util4calen.getWeekOfMonth(targetDay); 		// 월의 몇 번째 주 인지 추출. 예: 반환값이 4이면 (7월) 4번째 주
	   
		Date fweek = Util4calen.getFirstOfWeek(targetDay);		// 한주의 시작일자.
        Date lweek = Util4calen.getLastOfWeek(targetDay);		// 한주의 종료일자.
        Date preWeek = Util4calen.dateAdd(fweek, -1);			// 날자 계산: -1은 감소.
        Date nextWeek = Util4calen.dateAdd(lweek, 1);			// 날자 계산: -1은 감소.
        
        while(fweek.compareTo(lweek) <= 0) {
        	DateVO dvo = Util4calen.date2VO(fweek);
        	dvo.setIstoday(Util4calen.dateDiff(fweek, today) == 0);
        	calenList.add(dvo);
        	
        	fweek = Util4calen.dateAdd(fweek, 1);
        }
		return "main/index";
	}
}
