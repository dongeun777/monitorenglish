package igloosec.monitor.service;

import igloosec.monitor.mapper.BoardMapper;
import igloosec.monitor.vo.BoardVO;
import igloosec.monitor.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private BoardMapper boardmapper;

    @Autowired
    public BoardService(BoardMapper boardmapper) {
        this.boardmapper = boardmapper;

    }

    public List<BoardVO> boardListService() throws Exception{

        return boardmapper.boardList();
    }

    public BoardVO boardDetailService(int bno) throws Exception{

        return boardmapper.boardDetail(bno);
    }

    public void boardInsertService(BoardVO boardVO) throws Exception{
        boardmapper.boardInsert(boardVO);
    }



    public List<BoardVO> selectBoardList(BoardVO param ) {
        return boardmapper.selectBoardList(param);
    }


    public void boardUpdateService(BoardVO boardVO) throws Exception{

        boardmapper.boardUpdate(boardVO);
    }

    public void boardDeleteService(int bno) throws Exception{

        boardmapper.boardDelete(bno);
    }

    public void fileDeleteService(int bno) throws Exception{

        boardmapper.fileDelete(bno);
    }

    public void commentDeleteService(int bno) throws Exception{

        boardmapper.commentDelete(bno);
    }


    public void fileInsertService(FileVO fileVO) throws Exception{
        boardmapper.FileInsert(fileVO);
    }

    public FileVO fileDetailService(int bno) throws Exception{

        return boardmapper.fileDetail(bno);
    }


}
