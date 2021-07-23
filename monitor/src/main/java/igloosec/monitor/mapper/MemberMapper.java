package igloosec.monitor.mapper;


import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MemberMapper {

    MemberVo selectMember(String username);

    void joinMember(MemberVo memberVo);
    void createTmTable(MemberVo memberVo);
    void updatePw(MemberVo memberVo);
    void modifyGrpIpMember(MemberVo memberVo);
    void deleteMember(MemberVo memberVo);
    void deleteEquipList(MemberVo memberVo);
    UsageVo selectDeletePath();
    List<MemberVo> selectMemberList();
    void deleteFedTable(MemberVo memberVo);
    String selectPath();
    void deleteUserDiskUsage(MemberVo memberVo);

    void deleteUsedAmount(MemberVo memberVo);

    void deleteTmStats(MemberVo memberVo);

    List<MemberVo> selectUserProductInfoList(String email);

    boolean updateUserInfo(MemberVo memberVo);

    boolean updateUserDiskAutoscaling(MemberVo updateList);

    List<MemberVo> selectProductList();
}
