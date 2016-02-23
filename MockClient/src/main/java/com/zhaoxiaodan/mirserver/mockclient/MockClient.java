package com.zhaoxiaodan.mirserver.mockclient;

import com.zhaoxiaodan.mirserver.db.entities.User;
import com.zhaoxiaodan.mirserver.network.ClientPackets;
import com.zhaoxiaodan.mirserver.network.Packet;
import com.zhaoxiaodan.mirserver.network.ServerPackets;
import com.zhaoxiaodan.mirserver.network.debug.MyLoggingHandler;
import com.zhaoxiaodan.mirserver.network.decoder.ClientPacketBit6Decoder;
import com.zhaoxiaodan.mirserver.network.decoder.PacketDecoder;
import com.zhaoxiaodan.mirserver.network.encoder.PacketBit6Encoder;
import com.zhaoxiaodan.mirserver.network.encoder.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by liangwei on 16/2/19.
 */
public class MockClient {

	static final String HOST = "192.168.1.106";// "121.42.150.110";
	static final int    PORT = 7000;

	static short certification = 0;

	public void run() throws Exception {


		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group)
					.channel(NioSocketChannel.class)
					.handler(
							new ChannelInitializer<SocketChannel>() {
								@Override
								public void initChannel(SocketChannel ch) throws Exception {
									ch.pipeline().addLast(
											//编码
											new MyLoggingHandler(MyLoggingHandler.Type.Read),
											new DelimiterBasedFrameDecoder(1024, false, Unpooled.wrappedBuffer(new byte[]{'!'})),
											new ClientPacketBit6Decoder(),
											new MyLoggingHandler(MyLoggingHandler.Type.Read),
											new PacketDecoder(ServerPackets.class.getCanonicalName()),

											//解码
											new MyLoggingHandler(MyLoggingHandler.Type.Write),
											new PacketBit6Encoder(),
											new MyLoggingHandler(MyLoggingHandler.Type.Write),
											new PacketEncoder(),

											new MyLoggingHandler(MyLoggingHandler.Type.Read),
											new ChannelHandlerAdapter() {
												@Override
												public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
													if (msg instanceof ServerPackets.SelectServerOk) {
														certification = ((ServerPackets.SelectServerOk) msg).certification;
													}
												}
											}
									);
								}
							}
					);
			Channel ch = b.connect(HOST, PORT).sync().channel();

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			byte   cmdIndex = 0;
			Packet packet;
			User   user     = new User();
			user.loginId = "liang1";
			user.password = "liang1";
			user.username = "pangliang";

			// new user
//			packet = new ClientPackets.NewUser(cmdIndex,user);
//			ch.writeAndFlush(packet);
//			in.readLine();
//			cmdIndex = cmdIndex == 9?0:++cmdIndex;

			// login
			packet = new ClientPackets.Login(cmdIndex, user);
			ch.writeAndFlush(packet);
			in.readLine();
			cmdIndex = cmdIndex == 9 ? 0 : ++cmdIndex;

			//select server
			packet = new ClientPackets.SelectServer(cmdIndex, "家里测试");
			ch.writeAndFlush(packet);
			in.readLine();
			cmdIndex = cmdIndex == 9 ? 0 : ++cmdIndex;


			//****************   select server
			ch = b.connect(HOST, 7000).sync().channel();

			// new character
//			Character character = new Character();
//			character.user = user;
//			character.name = "pangliang";
//			character.hair = 1;
//			character.job = Job.Warrior;
//			character.gender = Gender.MALE;
//			packet = new ClientPackets.NewCharacter(cmdIndex,character);
//			ch.writeAndFlush(packet);
//			in.readLine();
//			cmdIndex = cmdIndex == 9?0:++cmdIndex;

			// query character
			packet = new ClientPackets.QueryCharacter(cmdIndex, user.loginId, certification);
			ch.writeAndFlush(packet);
			in.readLine();
			cmdIndex = cmdIndex == 9 ? 0 : ++cmdIndex;


			ch.closeFuture().sync();

		} finally {
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new MockClient().run();
	}
}
