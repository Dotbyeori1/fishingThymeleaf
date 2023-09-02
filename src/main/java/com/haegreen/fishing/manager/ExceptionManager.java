package com.haegreen.fishing.manager;

public class ExceptionManager {
    public static void iserror(boolean bool)throws Exception{
        if(!bool){throw new Exception();}
    }
}
