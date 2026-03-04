select member.user_uid,
       member.alias,
       member.role,
       user.avatar
from qt_group_member member
         inner join sys_user user on member.user_uid = user.uid
where group_id = 'Q594681250';

# 测试获取联系人申请表单信息
select r.from_uid    as fromUid,
       r.to_type     as toType,
       r.reason      as reason,
       r.status      as status,
       r.create_time as createTime,
       u.nick_name   as nickName,
       u.avatar      as avatar
from QTalk.qt_contact_request r
         left join QTalk.sys_user u on r.from_uid = u.uid
where r.contact_id = 'U127766356'
  and to_type = 1
order by r.create_time desc;

