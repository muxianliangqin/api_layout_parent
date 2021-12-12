package com.qiyue.test.demo.controller;


import com.qiyue.test.demo.model.vo.RoleVO;
import com.qiyue.test.demo.model.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping("selectUser/{id}")
    public UserVO selectUser(@PathVariable String id) {
        UserVO userVO = new UserVO();
        userVO.setId("user_" + id);
        userVO.setName("user");
        userVO.setRoleId("role:user");
        userVO.setGender("man");
        userVO.setAge(22);
        return userVO;
    }

    @GetMapping("selectRole/{id}")
    public RoleVO selectRole(@PathVariable String id) {
        RoleVO roleVO = new RoleVO();
        roleVO.setId("role_" + id);
        roleVO.setName("role");
        roleVO.setManagerId("managerId_" + id);
        roleVO.setDesc("this is a role");
        return roleVO;
    }


}
