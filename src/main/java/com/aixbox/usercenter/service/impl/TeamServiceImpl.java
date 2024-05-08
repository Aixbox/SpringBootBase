package com.aixbox.usercenter.service.impl;

import com.aixbox.usercenter.common.ErrorCode;
import com.aixbox.usercenter.exception.BusinessException;
import com.aixbox.usercenter.mapper.TeamMapper;
import com.aixbox.usercenter.model.VO.TeamUserVO;
import com.aixbox.usercenter.model.VO.UserVO;
import com.aixbox.usercenter.model.domain.Team;
import com.aixbox.usercenter.model.domain.User;
import com.aixbox.usercenter.model.domain.UserTeam;
import com.aixbox.usercenter.model.dto.TeamQuery;
import com.aixbox.usercenter.model.enums.TeamStatusEnum;
import com.aixbox.usercenter.model.request.TeamJoinRequest;
import com.aixbox.usercenter.model.request.TeamQuitRequest;
import com.aixbox.usercenter.model.request.TeamUpdateRequest;
import com.aixbox.usercenter.service.TeamService;
import com.aixbox.usercenter.service.UserService;
import com.aixbox.usercenter.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author he
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-03-30 15:07:08
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1.请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.是否登录、
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        final long userId = loginUser.getId();
        //校验信息
        //(1)队伍人数>1且<=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);

        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //(2)队伍标题<=20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }

        //(3) 描述<=512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述");
        }


        //(4)是否公开，默认为0
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum emunByValue = TeamStatusEnum.getEmunByValue(status);
        if (emunByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }

        //(5)如果status是加密状态，一定要密码，且密码<=32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(emunByValue)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }

        //(6)过期时间 <= 当前时间
        Date expiretime = team.getExpireTime();
        if (new Date().after(expiretime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间 <= 当前时间");
        }

        //(7)校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long count = this.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
        }

        //插入队伍消息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //插入用户到队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 获取队伍列表
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null) {
                queryWrapper.eq("id", id);
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }

            List<Long> idList = teamQuery.getIdList();
            if(!CollectionUtils.isEmpty(idList)){
                queryWrapper.in("id", idList);
            }

            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank((description))) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq("userId", userId);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum emunByValue = TeamStatusEnum.getEmunByValue(status);
            if(emunByValue == null){
                emunByValue = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && emunByValue.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if(status != null){
                queryWrapper.eq("status", status);
            }
        }
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        List<Team> list = this.list(queryWrapper);

        if(list == null){
            return new ArrayList<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : list) {
            Long userId1 = team.getUserId();
            if(userId1 == null){
                continue;
            }
            User user = userService.getById(userId1);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreatedUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeam(id);
        if(oldTeam.getUserId().longValue() != loginUser.getId().longValue() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEmunByValue(teamUpdateRequest.getStatus());
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要有密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeam(teamId);
        Date expireTime = team.getExpireTime();
        if(expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEmunByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(statusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }


        //分布式锁
        RLock lock = redissonClient.getLock("hobanpipei:join_team");
        try{
            while (true){
                if(lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)){
                    Long userId = loginUser.getId();
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", userId);
                    long hasJoinTeam = userTeamService.count(queryWrapper);
                    if(hasJoinTeam > 5){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多加入5个队伍");
                    }
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", userId);
                    queryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入队伍");
                    }
                    long teamHasJoinNum = getTeamHasJoinNum(teamId);
                    if(teamHasJoinNum >= team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }

                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e){
            return false;
        } finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

    /**
     * 获取队伍加入人数
     * @param teamId
     * @return
     */
    private long getTeamHasJoinNum(Long teamId) {
        QueryWrapper<UserTeam> queryWrapper;
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        return teamHasJoinNum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTime(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeam(teamId);
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("teamId", teamQuitRequest.getTeamId());
        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = getTeamHasJoinNum(teamId);
        if (teamHasJoinNum == 1) {
            this.removeById(teamId);
        } else {
            if(team.getUserId().longValue() == userId.longValue()){
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        return userTeamService.remove(queryWrapper);
    }

    private Team getTeam(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        Team team = getTeam(id);
        Long teamId = team.getId();
        if(team.getUserId().longValue() != loginUser.getId().longValue()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除队伍关系失败");
        }
        return this.removeById(teamId);
    }

}




