package com.tommyatkins.design.visitor.demo;

import com.tommyatkins.design.visitor.Vistor;

public class 時間よとまれ extends Song {

	public void rapInTheBegining() {

		System.out.println("Yeah!");
		System.out.println();
		System.out.println("君と過ごす時（とき）　あっという間");
		System.out.println("気つけばデートはもう終盤（しゅうばん）");
		System.out.println("離れたくない");
		System.out.println("帰りたくない");
		System.out.println("時を止めてずっと一緒にいたい");
		System.out.println();
		System.out.println("Yo!");

	}

	public void rapSoloInMiddle() {

		System.out.println("時間よとまれ　このまま");
		System.out.println("僕のたった一つワガママ　だけど");
		System.out.println("それは　叶（かな）わない");
		System.out.println("このも　どかしさ　伝（つた）えたいよ");
		System.out.println();
		System.out.println("一日　たった　二十四（にじゅうよん）時間");
		System.out.println("足（た）りない　あと百（ひゃく）時間");
		System.out.println();
		System.out.println("それだけあれば　もっともっと");
		System.out.println("みつけるよ　君のいいこと");

	}

	public void rapAnswerTheQuestionOfTheGirl() {

		System.out.println("Yo~");
		System.out.println("何度も　言ったらしつこく思うわれそう");
		System.out.println();
		System.out.println("不安と希望が半分ずつ");
		System.out.println("この思いは変わらない　多分ずっと");
		System.out.println("君は僕をどう思っているのか");

	}

	public void rapSwapWithTheSinger() {

		System.out.println("もし離れても　僕が繋（つな）ぎ止めるよ");
		System.out.println();
		System.out.println("Hey!");
		System.out.println();
		System.out.println("信じるよ　時が止まらなくても");
	}

	public void singNearlyTheFinal() {

		System.out.println("運命ならいつだって　巡り逢えるよね？");

	}

	public void rapAndSingInTheEnd() {

		System.out.println("Yeah~ yeah~");
		System.out.println("Every you~");
		System.out.println();
		System.out.println("Hey!");
		System.out.println();
		System.out.println("運命ならいつだって　巡り逢えるよね？");

	}

	@Override
	public void accept(Vistor vistor) {
		System.out.println("------------------------------------");
		this.rapInTheBegining();
		System.out.println("------------------------------------");
		this.rapSoloInMiddle();
		System.out.println("------------------------------------");
		this.rapAnswerTheQuestionOfTheGirl();
		System.out.println("------------------------------------");
		this.rapSwapWithTheSinger();
		System.out.println("------------------------------------");
		this.singNearlyTheFinal();
		System.out.println("------------------------------------");
		this.rapAndSingInTheEnd();
		System.out.println("------------------------------------");
	}

}
