package com.example.accountmanager;


import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;


public class Tutorial extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.TutorialBackground6)
                .page(new TitlePage(R.drawable.tutorial_icon,
                        "")
                )
                .page(new BasicPage(R.drawable.ic_baseline_list_24,
                        "アカウント情報管理",
                        "各サービスのアカウント情報を保存して管理できます。保存したアカウント情報の変更も簡単に行なえます。")
                        .background(R.color.TutorialBackground2)
                )
                .page(new BasicPage(R.drawable.ic_baseline_security_24,
                        "セキュリティ",
                        "生体認証とPINをサポートしています。PIN及びアカウント情報はAES暗号化方式で強固に守られます。")
                        .background(R.color.TutorialBackground7)
                )
                .page(new BasicPage(R.drawable.drop_box,
                        "バックアップと復元",
                        "DropBoxクラウドサービスと連携することでアカウント情報をバックアップ・復元できます。")
                        .background(R.color.TutorialBackground5)
                )
                .page(new BasicPage(R.drawable.ic_baseline_lock_24,
                        "PINの登録",
                        "最初にログイン用PINの登録を行ってください。")
                        .background(R.color.TutorialBackground1)
                )
                .swipeToDismiss(true)
                .exitAnimation(android.R.anim.slide_out_right)
                .build();
    }


}