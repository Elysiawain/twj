package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.pojo.Employee;
import com.tangwuji.reggie.service.EmployeeService;
import com.tangwuji.reggie.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//员工相关操作接口入口
@Slf4j
@RestController
@RequestMapping()//指定员工通用接口
@CrossOrigin
public class EmployeeController {

    //注入Service层
    @Autowired
    private EmployeeService employeeService;
    //员工登录请求接口
    @PostMapping("/employee/login")
    public R<Object> login(HttpServletRequest request, @RequestBody  Employee employee){//将请求数据封装进Employee实体类中
        log.info("员工登录请求");
        //1、对前端传来的密码进行md5加密
        String password = employee.getPassword();//获取前端密码，进行MD5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());//md5加密
        //2、根据前端传递的用户名查询数据库，如果查询到进行下一步，如果查询失败则直接返回错误信息
        log.info("准备查询数据库");
        //按用户名查询数据，这里就不用mybatis有问题
        //调用EmployeeService中的接口方法
        Employee employeeResult =employeeService.getByName(employee.getUsername());
        log.info("查询到员工信息{}",employeeResult);

        //3、判断是否查询到该用户名的用户
        if (employeeResult==null){
            return R.error("该用户不存在");
        }
        if (!employeeResult.getPassword().equals(password)) {//对比数据库密码
        //密码错误返回错误信息
            return R.error("密码错误");
        }
        if (employeeResult.getStatus()==0){//查询账号是否被禁用
            return R.error("该账号已被封禁，请联系管理员！");
        }
/*        //登陆成功将员工id存入Session并返回登陆成功
        HttpSession session=request.getSession();
        session.setAttribute("employee",employeeResult.getId());
        log.info("本次会话id：{}",request.getSession().getAttribute("employee"));
        request.getSession().setMaxInactiveInterval(60 * 60); // 设置会话时间为24小时*/

        //登陆成功下发jwt令牌
        //添加jwt令牌
        //先判断是否登录成功
        //登录成功下发jwt令牌
        //将用户数据存储一部分
        Map<String,Object> keyMap=new HashMap<>();
        keyMap.put("id",employeeResult.getId());
        keyMap.put("name",employeeResult.getName());
        //这里只存入姓名和年龄
        //调用方法,创建令牌
        String jwt = JwtUtil.createJwt(keyMap);
        log.info("令牌下发成功!");
        return R.success(employeeResult,jwt);//给前端返回jwt令牌
    }

    //员工退出功能
    @PostMapping("/employee/logout")
    public R<String> logout(HttpServletRequest request){
        //清除session
        log.info("用户退出，清除session");
        request.getSession().removeAttribute("employee");
        //清除数据库session

        return R.success(null,"退出成功！");
    }



    /**
     * 员工信息分页查询接口
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/employee/page")
    public R<Page> page(int page, int pageSize, String name){//接收前端参数，前端会传递当前也，每页展示数，搜索员工名
        log.info("页面：{}，每页展示数：{}，查询姓名：{}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo = new Page (page, pageSize) ;
        //构造条件构造器
//        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""), Employee.class);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper() ;
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee:: getUpdateTime);
        queryWrapper.notLike(Employee::getName, "超级管理员");
        //执行查询
         pageInfo = employeeService.page(pageInfo, queryWrapper);
        log.info("查询成功！");

        long ThreadId = Thread.currentThread().getId();
        log.info("当前线程ID为————：{}",ThreadId);
        return R.success(pageInfo,"success");//查询成功
    }

    //新增员工功能
    @PostMapping("/employee")//接口为employee这里就不需要再指定了
    public R<Object> saveEmployee(HttpServletRequest request,@RequestBody Employee employee){//前端传递数据为jSon格式
        String jwt = request.getParameter("token");
        log.info("jwt令牌:{}",jwt);
        //解析jwt令牌
        Claims parseJwt = JwtUtil.parseJwt(jwt);
        //查询现在登录的人员
        log.info("前端添加数据为：{}",employee);
        String  SelectUsername =employee.getUsername();

        //存入数据库前先校验该员工是否已经存在
       Employee checkEmp= employeeService.selectById(SelectUsername);
       if (checkEmp!=null){
           return R.error("该员工已经存在！");
       }

        //员工不存在开始执行添加操作
        Object id = parseJwt.get("id");//获取操作人的id
//用户id不需要管，添加员工初始密码默认为123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateUser(Long.valueOf(id.toString()));//设置创建人
        employee.setUpdateUser(Long.valueOf(id.toString()));//设置更新人
        //调用service
        employeeService.insertEmp(employee);//不需要返回
        log.info("添加成功！");
        return R.success("添加成功！","success");
    }

    //更新员工
    @PutMapping("/employee")
    public R<String>updateEmployee(HttpServletRequest request,@RequestBody Employee employee){
        long ThreadId = Thread.currentThread().getId();
        log.info("当前线程ID为————：{}",ThreadId);

        String jwt = request.getParameter("token");
        log.info("更新员工信息为：{}",employee);
        log.info("jwt令牌:{}",jwt);
        if (jwt!=null){
            Claims parseJwt = JwtUtil.parseJwt(jwt);
            Object updateId = parseJwt.get("id");
            employee.setUpdateUser(Long.valueOf(updateId.toString()));
        }

        //解析jwt令牌
        //Claims parseJwt = JwtUtil.parseJwt(jwt);
        //直接利用mybatis-plus的更新功能
        if (employee.getUsername()==null&&employee.getPhone()==null&&employee.getIdNumber()==null&&employee.getSex()==null){
            log.info("执行禁启用账号操作");
            employee.setUpdateUser(1L);
            employee.setUpdateTime(LocalDateTime.now());
            employeeService.updateById(employee);
            return R.success("更新成功！","success");
        }
        //如果账号重复，将更新失败，利用try_catch捕获
        try {
            log.info("执行更新员工数据操作");
            employeeService.updateById(employee);
            log.info("更新成功！");
        }catch(Exception e) {
            log.info("更新失败！");
            String message = e.getMessage();
            log.info(message);
            return R.error("该账号已存在！");
        }

        return R.success("更新成功！","success");
    }

    //回写员工数据
    @GetMapping("employee/{id}")
    public R<Employee>getEmployee(@PathVariable("id")Long id){
        log.info("回写员工数据");
        Employee emp = employeeService.getById(id);//利用MP
        return R.success(emp,"success");
    }
}
