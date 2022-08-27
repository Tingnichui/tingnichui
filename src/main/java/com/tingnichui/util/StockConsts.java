package com.tingnichui.util;

import java.util.NoSuchElementException;


/**
 * @author  Geng Hui
 * @date  2022/8/25 9:34
 */
public class StockConsts {

    public enum Exchange {
        SH("sh"), SZ("sz"), BJ("bj");
        private String name;

        private Exchange(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isSh() {
            return name.equals(Exchange.SH.name);
        }

        public boolean isSz() {
            return name.equals(Exchange.SZ.name);
        }

        public boolean isBj() {
            return name.equals(Exchange.BJ.name);
        }

        public static Exchange valueOfName(String name) {
            for (Exchange exchange : Exchange.values()) {
                if (exchange.name.equals(name)) {
                    return exchange;
                }
            }
            throw new NoSuchElementException("no exchange named " + name);
        }

    }


    public enum StockType {
        A(0), Index(1), ETF(2), B(3);
        private int value;

        private StockType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public enum StockLogType {
        New(0), Rename(1), Terminated(2);
        private int value;

        private StockLogType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

}
