package com.aixbox.usercenter.controller;

import com.aixbox.usercenter.common.BaseResponse;
import com.aixbox.usercenter.common.ErrorCode;
import com.aixbox.usercenter.common.ResultUtils;
import com.aixbox.usercenter.exception.BusinessException;
import com.aixbox.usercenter.model.domain.User;
import com.aixbox.usercenter.model.request.UserLoginRequest;
import com.aixbox.usercenter.model.request.UserRegisterRequest;
import com.aixbox.usercenter.model.request.matchUsersRequest;
import com.aixbox.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.aixbox.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author 魔王Aixbox
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 注册
     *
     * @param userRegisterRequest 用户注册对象
     * @return 响应对象
     */
//    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 前端注册校验
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long l = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(l);
    }

    /**
     * 登录
     *
     * @param userLoginRequest 用户登录对象
     * @param request          请求体
     * @return 响应对象
     */
    @PostMapping("login")
    public BaseResponse<User> userLogin(
            @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 退出登录
     *
     * @param request 请求体
     * @return 响应对象
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }

    /**
     * 根据用户名模糊搜索用户
     *
     * @param username 用户名
     * @param request  请求体
     * @return 响应对象
     */
    @GetMapping("/search")
    public BaseResponse searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> collect =
                userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        return ResultUtils.success(collect);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 响应对象
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(
            @RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 根据id删除用户
     *
     * @param id      用户id
     * @param request 请求体
     * @return 响应对象
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取当前用户信息
     *
     * @param request 请求体
     * @return 响应对象
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getCurrentUser(request));
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        int result = userService.updateUser(user, loginUser, request);
        return ResultUtils.success(result);
    }

    /**
     * 推荐列表
     *
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommendUsers(
            long pageNum, long pageSize, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        String redisKey = String.format("hobanpipei:user:recommend:%s", currentUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        List<User> userList = (List<User>) valueOperations.get(redisKey);
        // 有缓存，返回缓存数据
        if (userList != null) {
            return ResultUtils.success(userList);
        }
        // 没缓存,从数据库查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> list = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 脱敏
        List<User> collect =
                list.getRecords().stream()
                        .map(
                                (user -> {
                                    return userService.getSafetyUser(user);
                                }))
                        .collect(Collectors.toList());
        valueOperations.set(redisKey, collect, 300000, TimeUnit.MILLISECONDS);
        return ResultUtils.success(collect);
    }


    /**
     * 获取标签最匹配的用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(@RequestParam(defaultValue = "0") long num, HttpServletRequest request){
        if( num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        if(currentUser.getTags() == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "请设置标签");
        }
        return ResultUtils.success(userService.matchUsers(num, currentUser));
    }

    @PostMapping("register")
    public void reg(@RequestBody com.aixbox.usercenter.model.dto.User user){
        System.out.println(user.toString());
    }
}
