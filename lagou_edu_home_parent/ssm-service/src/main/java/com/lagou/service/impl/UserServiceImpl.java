package com.lagou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lagou.dao.UserMapper;
import com.lagou.domain.*;
import com.lagou.service.UserService;
import com.lagou.utils.Md5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;


    /*
        用户分页&多条件组合查询
     */
    @Override
    public PageInfo findAllUserByPage(UserVo userVo) {

        PageHelper.startPage(userVo.getCurrentPage(),userVo.getPageSize());
        List<User> allUserByPage = userMapper.findAllUserByPage(userVo);

        PageInfo<User> pageInfo = new PageInfo<>(allUserByPage);
        return pageInfo;
    }

    /*
        用户登陆
     */
    @Override
    public User login(User user) throws Exception {

        // 1.调用mapper方法 user2:包含了密文密码
        User user2 = userMapper.login(user);

        if(user2 !=null && Md5.verify(user.getPassword(),"lagou",user2.getPassword())){
            return user2;
        }else {
            return null;
        }

    }

    /*
        分配角色（回显）
     */
    @Override
    public List<Role> findUserRelationRoleById(Integer id) {
        List<Role> list = userMapper.findUserRelationRoleById(id);

        return list;
    }

    /*
        用户关联角色
     */
    @Override
    public void userContextRole(UserVo userVo) {

        // 1.根据用户ID清空中间表关联关系
        userMapper.deleteUserContextRole(userVo.getUserId());

        // 2.再从新建立关联关系
        for (Integer roleid : userVo.getRoleIdList()) {

            // 封装数据
            User_Role_relation user_role_relation = new User_Role_relation();
            user_role_relation.setUserId(userVo.getUserId());
            user_role_relation.setRoleId(roleid);

            Date date = new Date();
            user_role_relation.setCreatedTime(date);
            user_role_relation.setUpdatedTime(date);

            user_role_relation.setCreatedBy("system");
            user_role_relation.setUpdatedby("system");

            userMapper.userContextRole(user_role_relation);

        }

    }


    /*
        获取用户权限信息
     */
    @Override
    public ResponseResult getUserPermissions(Integer userid) {
        // 1. 根据当前用户id查询该用户拥有的角色
        List<Role> roles = userMapper.findUserRelationRoleById(userid);

        // 2.从角色集合中获取角色ID的集合
        List<Integer> roleIds = new ArrayList<>();
        for (Role role : roles) {
            roleIds.add(role.getId());
        }

        System.out.println(roleIds);

        // 3. 根据角色ID，查询角色所拥有的顶级菜单（-1）
        List<Menu> parentMenus = userMapper.findParentMenuByRoleId(roleIds);

        // 3. 根据菜单的parentID，查询子菜单信息
        for (Menu menu : parentMenus) {
            List<Menu> subMenuByPid = userMapper.findSubMenuByPid(menu.getId());
            menu.setSubMenuList(subMenuByPid);
        }

        // 4. 获取用户拥有的资源权限信息
        List<Resource> resourceByRoleId = userMapper.findResourceByRoleId(roleIds);

        // 5. 根据接口文档要求的格式封装数据并返回
        Map<String, Object> map = new HashMap<>();
        map.put("menuList", parentMenus);
        map.put("resourceList", resourceByRoleId);

        return new ResponseResult(true, 200, "获取用户信息权限成功", map);


    }




}
