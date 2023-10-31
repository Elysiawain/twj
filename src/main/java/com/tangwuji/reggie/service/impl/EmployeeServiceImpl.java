package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.mapper.EmployeeMapper;
import com.tangwuji.reggie.pojo.Employee;
import com.tangwuji.reggie.service.EmployeeService;
import com.tangwuji.reggie.utils.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
//这里同样继承MybatisPlus的实现类，并指定Mapper接口类
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;
    @Override
    public Employee getByName(String username) {

        return employeeMapper.selectByName(username);//直接调用mapper接口方法查询对应用户数据
    }
/*
* 按id来查询员工
* */
    @Override
    public Employee selectById(String username) {
        Employee employee = employeeMapper.selectByUsername(username);
        return employee;
    }

    /**
     * 添加员工
     * @param employee
     */
    @Override
    public void insertEmp(Employee employee) {
        //设置员工基本信息
        employee.setCreateTime(LocalDateTime.now());//创建日期
        employee.setUpdateTime(LocalDateTime.now());//最近更新日期
        //利用雪花算法计算id
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1); // 创建一个ID生成器，传入workerId和dataCenterId
        long id = idGenerator.generateId(); // 生成ID
        employee.setId(id);
        //添加进入数据库
        employeeMapper.insertEmp(employee.getId(),employee.getName(),employee.getUsername(),employee.getPassword(),employee.getPhone(),
                                  employee.getSex(),employee.getIdNumber(),employee.getCreateTime(),
                                  employee.getUpdateTime(),employee.getCreateUser(),employee.getUpdateUser() );
    }


}
