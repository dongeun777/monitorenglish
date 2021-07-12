package igloosec.monitor.mapper;


import igloosec.monitor.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ProductMapper {
    List<ProductVO> selectProductList();

    void productInsert(ProductVO productVO);

    int countProduct(ProductVO productVO);
}
