plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "cn.winfxk.android.mylibrary"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
        version = 2.0
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

publishing {
    repositories {
        maven {
            name = "LocalMaven"
            url = uri("file:///Y:/maven")
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "cn.winfxk.android.mylibrary"
                artifactId = "Winfxklia"
                version = "2.0.0"
                pom {
                    name.set("Winfxklia Library")
                    description.set("一个便于使用的Android类库")
                    url.set("http://Winfxk.cn")
                    developers {
                        developer {
                            id.set("Winfxk")
                            name.set("Winfxk")
                            email.set("admin@winfxk.cn")
                        }
                    }
                }
            }
        }
    }

    tasks.register("publishToAll") {
        group = "publishing"
        description = "同时发布到本地默认M2仓库和Y盘自定义Maven仓库"
        dependsOn(
            "publishReleasePublicationToMavenLocal",
            "publishReleasePublicationToLocalMavenRepository"
        )
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.camera.core)
    implementation(libs.camera.camera)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.okhttp)
    api(libs.winfxklib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.guava)
    implementation(libs.coil)
    implementation(libs.fastjson2)
    implementation(libs.fastjson2.extension)
}