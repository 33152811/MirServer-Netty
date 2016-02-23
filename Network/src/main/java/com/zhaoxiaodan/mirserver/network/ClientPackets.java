package com.zhaoxiaodan.mirserver.network;

import com.zhaoxiaodan.mirserver.db.entities.Character;
import com.zhaoxiaodan.mirserver.db.entities.Gender;
import com.zhaoxiaodan.mirserver.db.entities.Job;
import com.zhaoxiaodan.mirserver.db.entities.User;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

//TODO client 包现在写服务器部分,只实现读, 不管写
//TODO 按道理这里是网络封包模块不应该关联 DB 的 DAO, 当时包含的属性其实基本一致; 偷懒就这么用了
public class ClientPackets {

	public static final class Process extends IndexPacket {

		public Process(byte cmdIndex) {
			super(Protocol.CM_PROTOCOL, cmdIndex);
		}
	}

	public static final class Login extends IndexPacket {

		public User user;

		public Login() {
		}

		public Login(byte cmdIndex, User user) {
			super(Protocol.Login, cmdIndex);
			this.user = user;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);

			user = new User();
			String remain = readString(in);
			int    pos    = 0;
			if (remain != null && (pos = remain.indexOf(CONTENT_SEPARATOR_STR)) > 0) {
				user.loginId = remain.substring(0, pos);
				user.password = remain.substring(pos + 1);
			} else {
				throw new WrongFormatException();
			}
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);

			out.writeBytes(user.loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(user.password.getBytes());
		}
	}

	public static final class NewUser extends IndexPacket {

		public User user;

		public NewUser() {
		}

		public NewUser(byte cmdIndex, User user) {
			super(Protocol.NewUser, cmdIndex);
			this.user = user;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);

			user = new User();
			user.loginId = readString(in);
			user.password = readString(in);
			user.username = readString(in);
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(user.loginId.getBytes());
			out.writeBytes(new byte[]{0,0,0,0});
			out.writeBytes(user.password.getBytes());
			out.writeBytes(new byte[]{0,0,0,0});
			out.writeBytes(user.username.getBytes());
			out.writeBytes(new byte[]{0,0,0,0});

		}
	}

	public static final class SelectServer extends IndexPacket {

		public String serverName;

		public SelectServer() {}

		public SelectServer(byte cmdIndex, String serverName) {
			super(Protocol.SelectServer, cmdIndex);
			this.serverName = serverName;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			serverName = in.toString(Charset.defaultCharset()).trim();
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(serverName.getBytes());
		}
	}

	public static final class NewCharacter extends IndexPacket {

		public Character character;

		public NewCharacter() {}

		public NewCharacter(byte cmdIndex, Character character) {
			super(Protocol.NewCharacter, cmdIndex);
			this.character = character;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			String   content = in.toString(Charset.defaultCharset()).trim();
			String[] parts   = content.split(CONTENT_SEPARATOR_STR);
			if (parts.length < 5)
				throw new WrongFormatException();
			User user = new User();
			user.loginId = parts[0];
			character = new Character();
			character.user = user;
			character.name = parts[1];
			character.hair = Byte.parseByte(parts[2]);
			character.job = Job.values()[Byte.parseByte(parts[3])];
			character.gender = Gender.values()[Byte.parseByte(parts[4])];
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(character.user.loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(character.name.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeByte(character.hair + '0');
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeByte(character.job.ordinal() + '0');
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeByte(character.gender.ordinal() + '0');
			out.writeBytes(new byte[10]);
		}
	}

	public static final class QueryCharacter extends IndexPacket {

		public String loginId;
		public short certification;

		public QueryCharacter() {}

		public QueryCharacter(byte cmdIndex, String loginId, short certification) {
			super(Protocol.QueryCharacter, cmdIndex);
			this.loginId = loginId;
			this.certification = certification;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			String content = in.toString(Charset.defaultCharset()).trim();
			String[] parts = content.split(CONTENT_SEPARATOR_STR);
			if(parts.length >= 2)
			{
				loginId = parts[0];
				certification = Short.parseShort(parts[1]);
			}
		}


		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(Short.toString(certification).getBytes());
		}
	}

	public static final class DeleteCharacter extends IndexPacket {

		public String characterName;

		public DeleteCharacter() {}

		public DeleteCharacter(byte cmdIndex, String characterName) {
			super(Protocol.QueryCharacter, cmdIndex);
			this.characterName = characterName;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			this.characterName = in.toString(Charset.defaultCharset()).trim();
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(this.characterName.getBytes());
		}
	}
}
