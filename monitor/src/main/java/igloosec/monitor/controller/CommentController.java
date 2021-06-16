package igloosec.monitor.controller;


import igloosec.monitor.service.CommentService;
import igloosec.monitor.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class CommentController {

    private CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping("/comment/list") //댓글 리스트
    @ResponseBody
    private List<CommentVO> CommentServiceList(int bno) throws Exception{

        return commentService.commentListService(bno);
    }

    @RequestMapping("/comment/insert") //댓글 작성
    @ResponseBody
    private int CommentServiceInsert(@RequestParam int bno, @RequestParam String content, @RequestParam String writer) throws Exception{
        CommentVO comment = new CommentVO();
        comment.setBno(bno);
        comment.setContent(content);
        comment.setWriter(writer);

        return commentService.commentInsertService(comment);
    }

    @RequestMapping("/comment/update") //댓글 수정
    @ResponseBody
    private int CommentServiceUpdateProc(@RequestParam int cno, @RequestParam String content) throws Exception{

        CommentVO comment = new CommentVO();
        comment.setCno(cno);
        comment.setContent(content);

        return commentService.commentUpdateService(comment);
    }

    @RequestMapping("/comment/delete/{cno}") //댓글 삭제
    @ResponseBody
    private int CommentServiceDelete(@PathVariable int cno) throws Exception{

        return commentService.commentDeleteService(cno);
    }

}

