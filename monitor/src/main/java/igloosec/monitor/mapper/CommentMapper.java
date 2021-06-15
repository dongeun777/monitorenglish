package igloosec.monitor.mapper;

import igloosec.monitor.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    public int commentCount() throws Exception;


    public List<CommentVO> commentList(int bno) throws Exception;


    public int commentInsert(CommentVO commentVO) throws Exception;


    public int commentUpdate(CommentVO commentVO) throws Exception;


    public int commentDelete(int cno) throws Exception;

}


