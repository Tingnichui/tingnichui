package com.tingnichui.service;

public interface TradeService {

    Object health();

    Object buy() throws Exception;

    Object sell() throws Exception;

    Object cancel();

    Object getBalance() throws Exception;

    Object getBalanceDetail() throws Exception;
}
