select member.user_uid, member.alias, member.role , user.avatar
from qt_group_member member
inner join sys_user user on member.user_uid = user.uid
where group_id = 'Q594681250'

select *
from sys_user
