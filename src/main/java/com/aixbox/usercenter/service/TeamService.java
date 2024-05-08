package com.aixbox.usercenter.service;

import com.aixbox.usercenter.model.VO.TeamUserVO;
import com.aixbox.usercenter.model.domain.Team;
import com.aixbox.usercenter.model.domain.User;
import com.aixbox.usercenter.model.dto.TeamQuery;
import com.aixbox.usercenter.model.request.TeamJoinRequest;
import com.aixbox.usercenter.model.request.TeamQuitRequest;
import com.aixbox.usercenter.model.request.TeamUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
* @author he
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-03-30 15:07:08
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 获取队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 用户加入队伍
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTime(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);

}
