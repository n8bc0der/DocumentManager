package net.documents.docmanager.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
//import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.documents.docmanager.dao.DocumentDAO;
import net.documents.docmanager.model.Document;

@Controller
public class DocumentController {
	
	@Autowired
	private DocumentDAO documentDAO;
	
	@RequestMapping("/index")
	private String index(Map<String,Object> map) {
		try {
			map.put("document", new Document());
			map.put("documentList", documentDAO.list());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return "documents";
	}
	
	@RequestMapping(value="/save", method=RequestMethod.POST)
	public String save(@ModelAttribute("document") Document document,
								@RequestParam("file") MultipartFile file) {
		
		System.out.println("Name : " + document.getName());
		System.out.println("Description : " + document.getDescription());
		System.out.println("File : " + file.getName());
		System.out.println("ContentType : " + file.getContentType());
		
		try {
//			Blob blob = Hibernate.createBlob(file.getInputStream()); 
			//Session session = HibernateUtil.getSessionFactory().openSession();
			//Blob blob = Hibernate.getLobCreator(Session session).getSessionFactory().getCurrentSession()).createBlob(file.getInputStream());
			
			Blob blob = new javax.sql.rowset.serial.SerialBlob(IOUtils.toByteArray(file.getInputStream()));
			
			document.setFileName(file.getOriginalFilename());
			document.setContent(blob);
			document.setContentType(file.getContentType());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	
		return "redirect:/index.html";
	}
	
	@RequestMapping("/download/{documentId}")
	public String download(@PathVariable("documentId") Integer documentId,
								HttpServletResponse response) {
		
		Document doc = documentDAO.get(documentId);
		
		try {
				response.setHeader("Content-Disposition","inline;filename=\"" + doc.getFileName() + "\"");
				OutputStream out = response.getOutputStream();
				response.setContentType(doc.getContentType());
				IOUtils.copy(doc.getContent().getBinaryStream(), out);
				
				out.flush();
				out.close();
				
		}catch(IOException e) {
			e.printStackTrace();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@RequestMapping("/remove/{documentId")
	public String remove(@PathVariable("documentId") Integer documentId) {
		
		documentDAO.remove(documentId);
		
		return"redirect:/index.html";
	}

}
