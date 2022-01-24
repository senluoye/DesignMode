package com.example.qks;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 学校总部员工类
@Data
class Employee {
    private String id;
}

// 学院员工类
@Data
class CollegeEmployee {
    private String id;
}

// 学院员工管理类
class CollegeManager {
    //返回学院的所有员工
    public List<CollegeEmployee> getAllEmployee() {
        List<CollegeEmployee> list = new ArrayList<CollegeEmployee>();
        for (int i = 0; i < 10; i++) { //这里我们增加了 10 个员工到 list
            CollegeEmployee emp = new CollegeEmployee();
            emp.setId("学院员工 id= " + i);
            list.add(emp);
        }
        return list;
    }
    // 输出学院员工的信息
    public void printEmployee() {
        List<CollegeEmployee> list1 = getAllEmployee();
        System.out.println("------------学院员工------------");
        for (CollegeEmployee e : list1) {
            System.out.println(e.getId());
        }
    }
}

// 学校总部员工管理类
class SchoolManager {

    //该方法完成输出学校总部和学院员工id
    void printAllEmployee(CollegeManager sub) {
        sub.printEmployee();

        List<Employee> list2 = this.getAllEmployee();
        System.out.println("------------学校总部员工------------");
        for (Employee e : list2) {
            System.out.println(e.getId());
        }
    }

    //返回学校总部的员工id
    public List<Employee> getAllEmployee() {
        List<Employee> list = new ArrayList<Employee>();
        //这里我们增加了 5 个员工到 list
        for (int i = 0; i < 5; i++) {
            Employee emp = new Employee();
            emp.setId("学校总部员工 id= " + i);
            list.add(emp);
        }
        return list;
    }
}



public class example {
    public static void main(String[] args) {
        // 创建了一个 SchoolManager 对象
        SchoolManager schoolManager = new SchoolManager();
        // 输出学院的员工 id 和 学校总部的员工信息
        schoolManager.printAllEmployee(new CollegeManager());
    }
}
