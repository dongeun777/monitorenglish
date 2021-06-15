package igloosec.monitor.controller;

import igloosec.monitor.service.BoardService;
import igloosec.monitor.vo.BoardVO;
import igloosec.monitor.vo.FileVO;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Controller
public class BoardController {

    private BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }


//    @RequestMapping("/list") //게시판 리스트 화면 호출
//    private String boardList(Model model) throws Exception{
//
//        model.addAttribute("list", boardService.boardListService());
//
//        return "list";
//    }

//    @GetMapping("/insert")
//    public String insert(Model model, HttpServletRequest request) {
//        HttpSession session =request.getSession(false);
//        model.addAttribute("grpList", homeService.getGrpList());
//
//        if(session==null) return "redirect:/";
//        else return "insert";
//    }
//
//    @GetMapping("/list")
//    public String list(Model model, HttpServletRequest request) {
//        HttpSession session =request.getSession(false);
//        model.addAttribute("grpList", homeService.getGrpList());
//
//        if(session==null) return "redirect:/";
//        else return "list";
//    }


    @RequestMapping("/insert") //게시글 작성폼 호출
    private String boardInsertForm(){

        return "insert";
    }

    //@ResponseBody
    @RequestMapping("/detail") //게시글 작성폼 호출
    private String detail(BoardVO param , Model model) throws Exception{
        int bno = param.getBno();

        model.addAttribute("DETAIL", boardService.boardDetailService(bno));
        model.addAttribute("files", boardService.fileDetailService(bno));


        return "detail";
    }

    @RequestMapping(value = "/insertProc")
    private String boardInsertProc(HttpServletRequest request,@RequestPart MultipartFile files) throws Exception{


        BoardVO board = new BoardVO();
        FileVO file  = new FileVO();

        board.setSubject(request.getParameter("subject"));
        board.setContent(request.getParameter("content"));
        board.setWriter(request.getParameter("writer"));


        if(files.isEmpty()){ //업로드할 파일이 없을 시
            boardService.boardInsertService(board); //게시글 insert
        }else{
            String fileName = files.getOriginalFilename();
            String fileNameExtension = FilenameUtils.getExtension(fileName).toLowerCase();
            File destinationFile;
            String destinationFileName;
            System.out.println(fileName);
            System.out.println(fileNameExtension);
            String fileUrl = "C:\\Users\\user\\Desktop\\igloosecmonitor\\monitor\\src\\main\\java\\igloosec\\monitor\\uploadFiles\\";

            do {
                destinationFileName = RandomStringUtils.randomAlphanumeric(32) + "." + fileNameExtension;
                destinationFile = new File(fileUrl+ destinationFileName);
            } while (destinationFile.exists());

            destinationFile.getParentFile().mkdirs();
            files.transferTo(destinationFile);

            boardService.boardInsertService(board); //게시글 insert

            file.setBno(board.getBno());
            System.out.println(board.getBno());
            file.setFilename(destinationFileName);
            file.setFileOriName(fileName);
            file.setFileurl(fileUrl);

            boardService.fileInsertService(file); //file insert
        }


        return "redirect:/list";

    }

    @ResponseBody
    @RequestMapping(value = "/getBoardList.do")
    public List<BoardVO> getBoardList(Model model, BoardVO param)  {


        List<BoardVO> list = boardService.selectBoardList(param);

        model.addAttribute("list",list);

        return list;
    }

    @RequestMapping("/update") //게시글 수정폼 호출
    private String boardUpdateForm(BoardVO param, Model model) throws Exception{
        int bno = param.getBno();

        model.addAttribute("DETAIL", boardService.boardDetailService(bno));

        return "update";
    }



    @RequestMapping("/updateProc")
    private String boardUpdateProc(HttpServletRequest request) throws Exception{

        BoardVO board = new BoardVO();
        board.setSubject(request.getParameter("subject"));
        board.setContent(request.getParameter("content1"));
        board.setBno(Integer.parseInt(request.getParameter("bno")));

        boardService.boardUpdateService(board);

        return "redirect:/detail?bno="+ request.getParameter("bno");
    }


    @RequestMapping("/delete")
    private String boardDelete(BoardVO param, Model model) throws Exception{
        int bno = param.getBno();

        boardService.boardDeleteService(bno);
        boardService.fileDeleteService(bno);

        return "redirect:/list";
    }

    @RequestMapping("/fileDown/{bno}")
    private void fileDown(@PathVariable int bno, HttpServletRequest request, HttpServletResponse response) throws Exception{

        request.setCharacterEncoding("UTF-8");
        FileVO fileVO = boardService.fileDetailService(bno);

        //파일 업로드된 경로
        try{
            String fileUrl = fileVO.getFileurl();
            fileUrl += "/";
            String savePath = fileUrl;
            String fileName = fileVO.getFilename();

            //실제 내보낼 파일명
            String oriFileName = fileVO.getFileOriName();
            InputStream in = null;
            OutputStream os = null;
            File file = null;
            boolean skip = false;
            String client = "";

            //파일을 읽어 스트림에 담기
            try{
                file = new File(savePath, fileName);
                in = new FileInputStream(file);
            } catch (FileNotFoundException fe) {
                skip = true;
            }

            client = request.getHeader("User-Agent");

            //파일 다운로드 헤더 지정
            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Description", "JSP Generated Data");

            if (!skip) {
                // IE
                if (client.indexOf("MSIE") != -1) {
                    response.setHeader("Content-Disposition", "attachment; filename=\""
                            + java.net.URLEncoder.encode(oriFileName, "UTF-8").replaceAll("\\+", "\\ ") + "\"");
                    // IE 11 이상.
                } else if (client.indexOf("Trident") != -1) {
                    response.setHeader("Content-Disposition", "attachment; filename=\""
                            + java.net.URLEncoder.encode(oriFileName, "UTF-8").replaceAll("\\+", "\\ ") + "\"");
                } else {
                    // 한글 파일명 처리
                    response.setHeader("Content-Disposition",
                            "attachment; filename=\"" + new String(oriFileName.getBytes("UTF-8"), "ISO8859_1") + "\"");
                    response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
                }
                response.setHeader("Content-Length", "" + file.length());
                os = response.getOutputStream();
                byte b[] = new byte[(int) file.length()];
                int leng = 0;
                while ((leng = in.read(b)) > 0) {
                    os.write(b, 0, leng);
                }
            } else {
                response.setContentType("text/html;charset=UTF-8");
                System.out.println("<script language='javascript'>alert('파일을 찾을 수 없습니다');history.back();</script>");
            }
            in.close();
            os.close();
        } catch (Exception e) {
            System.out.println("ERROR : " + e.getMessage());
        }

    }




}
