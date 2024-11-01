package ru.mtuci.demo.service;

import ru.mtuci.demo.model.Demo;

import java.util.List;

public interface DemoService {
    void save(Demo demo);
    List<Demo> findAll();
    Demo findById(long id);
}
