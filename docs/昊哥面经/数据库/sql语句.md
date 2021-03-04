**排序**

Order by [字段1] [DESC], [字段2] [DESC];

**分页查询**

LIMIT 1 OFFSET 2 ; 从第二条记录开始，查询最多1条记录

**聚合**

count(*)

查询人数

select class_id, gender, COUNT(*) num from students GROUP BY class_id, gender;

//查询每个班级每种性别的人数

**Insert**

```
INSERT INTO <表名> (字段1, 字段2, ...) VALUES (值1, 值2, ...);
```

**UPDATE**

```
UPDATE <表名> SET 字段1=值1, 字段2=值2, ... WHERE ...;
```

**DELETE**

```
DELETE FROM <表名> WHERE ...;
```

**having 和 where的区别**

where是从数据表中的字段直接进行的筛选的。

having是从 前面筛选的字段再筛选,可以使用字段别名，而where不能使用

having能够使用统计函数，先分组，再判断(having)，但是where不能使用

**DISTINICT**

去重

 对于distinct与group by的使用: 1、当对系统的性能高并数据量大时使用group by 2、当对系统的性能不高时使用数据量少时两者皆可 3、尽量使用group by 

**ifnull((), null)** 输出null字段