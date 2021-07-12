package igloosec.monitor.controller;

import igloosec.monitor.service.ProductService;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
public class ProductController {

    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @ResponseBody
    @RequestMapping(value = "/getProductList.do")
    public List<ProductVO> getProductList(Model model)  {

        List<ProductVO> list = productService.selectProductList();

        model.addAttribute("list",list);

        return list;
    }

    @ResponseBody
    @RequestMapping(value = "/InsertProduct")
    public String InsertProduct(ProductVO productVO, HttpSession session) {

        String emailStr = (String) session.getAttribute("email");
        productVO.setApply_id(emailStr + UUID.randomUUID().toString().replace("-",""));
        productVO.setEmail(emailStr);
        session.setAttribute("pStep",0);
        session.setAttribute("apply_id",productVO.getApply_id());
        int count = productService.countProduct(productVO);

        if(count == 0)
            count = 1;
        else
            count += 1;

        String Count = Integer.toString(count);
        String pdName = productVO.getProd_id() + "_" + Count;
        productVO.setProductName(pdName);
        productService.productInsert(productVO);


        return "Success";

    }
}
