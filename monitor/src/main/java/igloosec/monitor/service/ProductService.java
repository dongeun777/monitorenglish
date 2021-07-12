package igloosec.monitor.service;

import igloosec.monitor.mapper.CostMapper;
import igloosec.monitor.mapper.ProductMapper;
import igloosec.monitor.vo.CostVo;
import igloosec.monitor.vo.ProductVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    public final ProductMapper mapper;

    public ProductService(ProductMapper mapper) {
        this.mapper = mapper;

    }

    public List<ProductVO> selectProductList() {
        return mapper.selectProductList();
    }

    public void productInsert(ProductVO productVO) {
        mapper.productInsert(productVO);
    }

    public int countProduct(ProductVO productVO) {
        return mapper.countProduct(productVO);
    }
}
