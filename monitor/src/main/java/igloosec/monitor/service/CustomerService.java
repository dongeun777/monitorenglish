package igloosec.monitor.service;


import igloosec.monitor.mapper.CustomerMapper;
import igloosec.monitor.mapper.MemberMapper;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CustomerService {
    public final CustomerMapper mapper;

    public CustomerService(CustomerMapper mapper) {
        this.mapper = mapper;
    }


    public List<MemberVo> selectMemberList(UsageVo param) {
        return mapper.selectMemberList(param);
    }



}