package hu.common;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {
	static final Logger LOGGER = LoggerFactory.getLogger(AdminInterceptor.class);
	static final Integer IMG_WIDTH = 100;
	static final Integer IMG_HEIGHT = 100;
	
	/**
	 * 멀티 파일 업로드.
	 */
	public List<FileVO> saveAllFiles(List<MultipartFile> upfiles) {
		List<FileVO> filelist = new ArrayList<FileVO>();
		String filePath = LocaleMessage.getMessage("info.filePath");
		
		for(MultipartFile uploadfile : upfiles) {
			if(uploadfile.getSize() == 0) {
				continue;
			}
			
			String newName = getNewName();
			
			saveFileOne(uploadfile, getRealPath(filePath, newName), newName);
			
			FileVO filedo = new FileVO();
			filedo.setFilename(uploadfile.getOriginalFilename());
			filedo.setRealname(newName);
			filedo.setFilesize(uploadfile.getSize());
			filelist.add(filedo);
		}
		
		return filelist;
	}
	
	/**
	 * 실제 파일 저장
	 */
	public static String saveFileOne(MultipartFile file, String basePath, String fileName) {
		if(file == null || file.getName().equals("") || file.getSize() < 1) {
			return null;
		}
		
		makeBasePath(basePath);
		String serverFullPath = basePath + fileName;
		
		File file1 = new File(serverFullPath);
		try {
			file.transferTo(file1);
		} catch(IOException ex) {
			LOGGER.error("IOException");
		}
		
		return serverFullPath;
	}
	
	/**
	 * 파일 저장 경로 생성.
	 */
	public static void makeBasePath(String path) {
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	
	
	/**
	 * 날짜로 새로운 파일명 주기
	 */
	public static String getNewName() {
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		return ft.format(new Date()) + (int) (Math.random() * 10);
	}
	
	public String getRealPath(String path, String filename) {
		return path + filename.substring(0,4) + "/";
	}
}
