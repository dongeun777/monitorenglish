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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        Date today = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "yyyyMMddHHmm";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);


        String emailStr = (String) session.getAttribute("email");
        productVO.setApply_id(emailStr + formatter.format(today));
        productVO.setEmail(emailStr);
        session.setAttribute("pStep",0);
        session.setAttribute("apply_id",productVO.getApply_id());
        String rscgrp  = productVO.getApply_id().replace("@","");
        rscgrp = rscgrp.replace(".","");

        int count = productService.countProduct(productVO);

        if(count == 0)
            count = 1;
        else
            count += 1;

        String Count = Integer.toString(count);
        String pdName = productVO.getProd_id() + "_" + Count;
        productVO.setProductName(pdName);
        productVO.setRscgrp(rscgrp);
        productService.productInsert(productVO);


        return "Success";

    }
}
