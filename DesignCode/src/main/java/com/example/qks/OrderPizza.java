package com.example.qks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OrderPizza {
    public OrderPizza() throws IOException {
        Pizza pizza = null;
        String orderType; // 订购披萨的类型
        int label = 0;
        do {
            orderType = getType();
            switch (orderType) {
                case "greek" -> {
                    pizza = new GreekPizza();
                    pizza.setName("希腊披萨");
                }
                case "cheese" -> {
                    pizza = new CheesePizza();
                    pizza.setName("奶酪披萨");
                }
                case "pepper" -> {
                    pizza = new PepperPizza();
                    pizza.setName("胡椒披萨");
                }
                default -> label = 1;
            }
            //输出 pizza 制作过程
            pizza.prepare();
            pizza.bake();
            pizza.cut();
            pizza.box();

        } while (label != 0);
    }

    // 从控制台获取Pizza类型
    private String getType() throws IOException {
        BufferedReader str;
        try {
            str = new BufferedReader(new InputStreamReader(System.in));
            return str.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
