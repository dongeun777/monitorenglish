package igloosec.monitor.service;

import igloosec.monitor.mapper.CommentMapper;
import igloosec.monitor.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;

    }

    public List<CommentVO> commentListService(int bno) throws Exception{

        return commentMapper.commentList(bno);
    }

    public int commentInsertService(CommentVO comment) throws Exception{

        return commentMapper.commentInsert(comment);
    }

    public int commentUpdateService(CommentVO comment) throws Exception{

        return commentMapper.commentUpdate(comment);
    }

    public int commentDeleteService(int cno) throws Exception{

        return commentMapper.commentDelete(cno);
    }
}


