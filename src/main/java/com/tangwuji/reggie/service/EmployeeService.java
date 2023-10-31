package com.tangwuji.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tangwuji.reggie.pojo.Employee;

public interface EmployeeService extends IService<Employee> {
    //根据用户名查找员工，并返回查询到的数据
    Employee getByName(String username);

    //查询员工是否已经存在
    Employee selectById(String username);

    void insertEmp(Employee employee);

}
