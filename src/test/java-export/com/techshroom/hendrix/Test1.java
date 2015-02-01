package com.techshroom.hendrix;

import java.util.List;

public class Test1 {
    private final List<Object> objectList = null;
    private final List<Test1> test1List = null;
    private final Object object = null;

    public Test1() {}
    
    public List<Object> getObjectList() {
        return objectList;
    }
    
    public int getLengthOfTest1ListTimes(int amount) {
        return test1List.size() * amount;
    }
}
