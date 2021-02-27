package com.rain.advance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Program: rain-java-ideas
 * @Description: 实现一致性缓存，不用重复性的去数据库中查询
 * @Author: HouHao Ye
 * @Create: 2021-02-27 18:56
 **/
public class CoherentCache {
    class GenericCachedDao<T> {
        // HashMap 作为缓存非线程安全, 需要保护
        HashMap<SqlPair, T> map = new HashMap<>();
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        GenericDao genericDao = new GenericDao();

        public int update(String sql, Object... params) {
            SqlPair key = new SqlPair(sql, params);
            // 加写锁, 防止其它线程对缓存读取和更改
            lock.writeLock().lock();
            try {
                int rows = genericDao.update(sql, params);
                map.clear();
                return rows;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public T queryOne(Class<T> beanClass, String sql, Object... params) {
            SqlPair key = new SqlPair(sql, params);
            // 加读锁, 防止其它线程对缓存更改
            lock.readLock().lock();
            try {
                T value = map.get(key);
                if (value != null) {
                    return value;
                }
            } finally {
                lock.readLock().unlock();
            }

            // 加写锁, 防止其它线程对缓存读取和更改
            lock.writeLock().lock();
            try {
                // 注意！！！get 方法上面部分是可能多个线程进来的, 可能已经向缓存填充了数据
                // 为防止重复查询数据库, 再次验证
                T value = map.get(key);
                if (value == null) {
                    // 如果没有, 查询数据库
                    value = (T) genericDao.queryOne(beanClass, sql, params);
                    map.put(key, value);
                }
                return value;
            } finally {
                lock.writeLock().unlock();
            }
        }

        // 作为 key 保证其是不可变的
        class SqlPair {
            private String sql;
            private Object[] params;

            public SqlPair(String sql, Object[] params) {
                this.sql = sql;
                this.params = params;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                SqlPair sqlPair = (SqlPair) o;
                return sql.equals(sqlPair.sql) &&
                        Arrays.equals(params, sqlPair.params);
            }

            @Override
            public int hashCode() {
                int result = Objects.hash(sql);
                result = 31 * result + Arrays.hashCode(params);
                return result;
            }
        }

    }
}


class GenericDao<T> {
    private static final Object T = null;

    public int update(String sql, Object[] params) {
        return 0;
    }

    public <T> T queryOne(Class<T> beanClass, String sql, Object[] params) {
        return (T) T;
    }
}