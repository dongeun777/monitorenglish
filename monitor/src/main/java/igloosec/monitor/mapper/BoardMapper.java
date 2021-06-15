package igloosec.monitor.mapper;

import igloosec.monitor.vo.BoardVO;
import igloosec.monitor.vo.FileVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface BoardMapper {
    //게시글 개수
    public int boardCount() throws Exception;

    //게시글 목록
    public List<BoardVO> boardList() throws Exception;

    //게시글 상세
    BoardVO boardDetail(int bno) throws Exception;

    //게시글 작성
    void boardInsert(BoardVO boardVO) throws Exception;

    //게시글 수정
    void boardUpdate(BoardVO board) throws Exception;

    //게시글 삭제
    void boardDelete(int bno) throws Exception;

    void fileDelete(int bno) throws Exception;

    List<BoardVO> selectBoardList(BoardVO param);

    public void FileInsert(FileVO fileVO) throws Exception;

    public FileVO fileDetail(int bno) throws Exception;

}
