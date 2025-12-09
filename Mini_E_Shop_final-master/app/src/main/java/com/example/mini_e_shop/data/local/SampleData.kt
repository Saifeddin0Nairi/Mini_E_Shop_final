package com.example.mini_e_shop.data.local

import com.example.mini_e_shop.data.local.entity.ProductEntity

object SampleData {
    fun getSampleProducts(): List<ProductEntity> {
        return listOf(
            ProductEntity(
                id = "0",
                name = "iPhone 14 Pro", brand = "Apple", category = "Điện thoại",
                origin = "USA", price = 999.0, stock = 50,
                imageUrl = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-14-pro-model-unselect-gallery-2-202209_GEO_US?wid=5120&hei=2880&fmt=p-jpg&qlt=80&.v=1660753617560",
                description = "iPhone 14 Pro with Dynamic Island."
            ),
            ProductEntity(
                id = "1",
                name = "Samsung Galaxy S23 Ultra", brand = "Samsung", category = "Điện thoại",
                origin = "Korea", price = 1199.0, stock = 40,
                imageUrl = "https://www.androidauthority.com/wp-content/uploads/2023/02/samsung-galaxy-s23-ultra-hero.jpg",
                description = "The ultimate Galaxy phone with a built-in S Pen."
            ),
            ProductEntity(
                id = "2",
                name = "MacBook Air M2", brand = "Apple", category = "Laptop",
                origin = "USA", price = 1199.0, stock = 30,
                imageUrl = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/macbook-air-midnight-select-20220606?wid=904&hei=840&fmt=jpeg&qlt=90&.v=1652836879941",
                description = "Supercharged by the M2 chip."
            ),
            ProductEntity(
                id = "3",
                name = "Dell XPS 15", brand = "Dell", category = "Laptop",
                origin = "USA", price = 1499.0, stock = 25,
                imageUrl = "https://tse2.mm.bing.net/th/id/OIP.W818awHtE8yQN_zufAyJjAHaFj?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "Stunning display and powerful performance."
            ),
            ProductEntity(
                id = "4",
                name = "Sony WH-1000XM5", brand = "Sony", category = "Tai nghe",
                origin = "Japan", price = 399.0, stock = 60,
                imageUrl = "https://www.bhphotovideo.com/images/images2000x2000/sony_wh1000xm5_s_wh_1000xm5_noise_canceling_wireless_over_ear_1706394.jpg",
                description = "Industry-leading noise canceling headphones."
            ),
            ProductEntity(
                id = "5",
                name = "Apple Watch Series 8", brand = "Apple", category = "Đồng hồ",
                origin = "USA", price = 399.0, stock = 70,
                imageUrl = "https://tse4.mm.bing.net/th/id/OIP.1L2PhweBpQgIgVA45n_G7QHaG8?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "A healthy leap ahead."
            ),
            ProductEntity(
                id = "6",
                name = "Logitech MX Master 3S", brand = "Logitech", category = "Phụ kiện",
                origin = "Switzerland", price = 99.0, stock = 100,
                imageUrl = "https://resource.logitech.com/w_800,c_lpad,ar_1:1,q_auto,f_auto,dpr_1.0/d_transparent.gif/content/dam/logitech/en/products/mice/mx-master-3s/gallery/mx-master-3s-mouse-top-view-graphite.png?v=1",
                description = "Iconic mouse, remastered."
            ),
            ProductEntity(
                id = "7",
                name = "Canon EOS R6 Mark II", brand = "Canon", category = "Máy ảnh",
                origin = "Japan", price = 2499.0, stock = 15,
                imageUrl = "https://www.cined.com/content/uploads/2022/11/Canon-R6-Mark-II-review.jpg",
                description = "A hybrid powerhouse for stills and video."
            ),
            ProductEntity(
                id = "8",
                name = "Nintendo Switch OLED", brand = "Nintendo", category = "Gaming",
                origin = "Japan", price = 349.0, stock = 45,
                imageUrl = "https://tse4.mm.bing.net/th/id/OIP.ePWj5sQmYNRUsKG_lEqsbAHaFD?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "Play at home or on the go."
            ),
            ProductEntity(
                id = "9",
                name = "GoPro HERO11 Black", brand = "GoPro", category = "Máy ảnh",
                origin = "USA", price = 499.0, stock = 35,
                imageUrl = "https://tse3.mm.bing.net/th/id/OIP.4UqP0zNbNPA_PJ0yToAw9gHaFW?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "The most versatile GoPro yet."
            ),
            ProductEntity(
                id = "10",
                name = "iPad Pro M2", brand = "Apple", category = "Tablet",
                origin = "USA", price = 799.0, stock = 30,
                imageUrl = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-11-select-wifi-spacegray-202210?wid=940&hei=1112&fmt=p-jpg&qlt=95&.v=1664411231432",
                description = "The ultimate iPad experience."
            ),
            ProductEntity(
                id = "11",
                name = "Samsung Odyssey G9", brand = "Samsung", category = "Màn hình",
                origin = "Korea", price = 1399.0, stock = 20,
                imageUrl = "https://i0.wp.com/9to5toys.com/wp-content/uploads/sites/5/2023/06/Samsung-Odyssey-OLED-G9-lead.jpg?resize=1200%2C628&ssl=1",
                description = "49-inch curved gaming monitor."
            ),
            ProductEntity(
                id = "12",
                name = "AirPods Pro (2nd gen)", brand = "Apple", category = "Tai nghe",
                origin = "USA", price = 249.0, stock = 80,
                imageUrl = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MQD83?wid=1144&hei=1144&fmt=jpeg&qlt=90&.v=1660803972361",
                description = "Rebuilt for more immersive sound."
            ),
            ProductEntity(
                id = "13",
                name = "Razer BlackWidow V4 Pro", brand = "Razer", category = "Bàn phím",
                origin = "USA", price = 229.0, stock = 50,
                imageUrl = "https://mlpnk72yciwc.i.optimole.com/cqhiHLc.IIZS~2ef73/w:auto/h:auto/q:75/https://bleedingcool.com/wp-content/uploads/2023/02/Razer-BlackWidow-V4-Pro-Gaming.jpg",
                description = "A mechanical gaming keyboard."
            ),
            ProductEntity(
                id = "14",
                name = "Anker 737 Power Bank", brand = "Anker", category = "Phụ kiện",
                origin = "China", price = 149.0, stock = 90,
                imageUrl = "https://cdn.shopify.com/s/files/1/0493/9834/9974/products/A1289011-Anker_737_Power_Bank_PowerCore_24K_1_3840x.png?v=1672388225",
                description = "24,000mAh 140W portable charger."
            ),
            ProductEntity(
                id = "15",
                name = "Google Pixel 7 Pro", brand = "Google", category = "Điện thoại",
                origin = "USA", price = 899.0, stock = 40,
                imageUrl = "https://tse3.mm.bing.net/th/id/OIP.j5RlQwxWOyQq_mD7UPg3PAHaFj?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "The all-pro Google phone."
            ),
            ProductEntity(
                id = "16",
                name = "Bose QuietComfort Earbuds II", brand = "Bose", category = "Tai nghe",
                origin = "USA", price = 299.0, stock = 55,
                imageUrl = "https://tse1.explicit.bing.net/th/id/OIP.4HqGINcybokOQOmOd58kVgHaE7?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "The world's best noise cancellation."
            ),
            ProductEntity(
                id = "17",
                name = "Kindle Paperwhite", brand = "Amazon", category = "Tablet",
                origin = "USA", price = 139.0, stock = 120,
                imageUrl = "https://tse2.mm.bing.net/th/id/OIP.5mdaA5NNf3SJqbTkS3QDQgHaFj?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3",
                description = "Read anytime, anywhere."
            ),
            ProductEntity(
                id = "18",
                name = "Elgato Stream Deck MK.2", brand = "Elgato", category = "Phụ kiện",
                origin = "Germany", price = 149.0, stock = 60,
                imageUrl = "https://tse3.mm.bing.net/th/id/OIP.Pl-hYD2glnFBepT3WcvLdQHaD-?w=314&h=180&c=7&r=0&o=7&cb=ucfimg2&dpr=1.3&pid=1.7&rm=3&ucfimg=1",
                description = "15 customizable LCD keys for streamers."
            ),
            ProductEntity(
                id = "19",
                name = "Sony a7 IV", brand = "Sony", category = "Máy ảnh",
                origin = "Japan", price = 2498.0, stock = 20,
                imageUrl = "https://th.bing.com/th/id/R.f740fe45e5ca33c56221220e169d10ac?rik=6GVwcRVehfDFQA&pid=ImgRaw&r=0",
                description = "A7 IV: Beyond basic."
            ),
            // Thêm 20 sản phẩm mới
            ProductEntity(
                id = "20",
                name = "Xiaomi 13 Pro", brand = "Xiaomi", category = "Điện thoại",
                origin = "China", price = 899.0, stock = 45,
                imageUrl = "https://cdn.shopify.com/s/files/1/0602/6701/8712/products/xiaomi-13-pro-5g-12gb-256gb-black_1200x1200.jpg",
                description = "Flagship smartphone với camera Leica."
            ),
            ProductEntity(
                id = "21",
                name = "OnePlus 11", brand = "OnePlus", category = "Điện thoại",
                origin = "China", price = 699.0, stock = 50,
                imageUrl = "https://www.oneplus.com/content/dam/oasis/page/2023/02/oneplus-11/oneplus-11-product-image.png",
                description = "Never Settle - Flagship performance."
            ),
            ProductEntity(
                id = "22",
                name = "ASUS ROG Zephyrus G15", brand = "ASUS", category = "Laptop",
                origin = "Taiwan", price = 1799.0, stock = 20,
                imageUrl = "https://dlcdnwebimgs.asus.com/gain/8F2A2A5C-0A5F-4B0A-8A5C-5A5F5A5F5A5F",
                description = "Gaming laptop mạnh mẽ với RTX 4070."
            ),
            ProductEntity(
                id = "23",
                name = "HP Spectre x360", brand = "HP", category = "Laptop",
                origin = "USA", price = 1299.0, stock = 30,
                imageUrl = "https://ssl-product-images.www8-hp.com/digmedialib/prodimg/lowres/c08286648.png",
                description = "Premium 2-in-1 convertible laptop."
            ),
            ProductEntity(
                id = "24",
                name = "JBL Flip 6", brand = "JBL", category = "Tai nghe",
                origin = "USA", price = 129.0, stock = 80,
                imageUrl = "https://www.jbl.com/dw/image/v2/BFND_PRD/on/demandware.static/-/Sites-masterCatalog_Harman/default/dw0a8c8e0e/JBL_FLIP6_BLK_001_dynamic.png",
                description = "Portable Bluetooth speaker với âm thanh mạnh mẽ."
            ),
            ProductEntity(
                id = "25",
                name = "Garmin Forerunner 955", brand = "Garmin", category = "Đồng hồ",
                origin = "USA", price = 599.0, stock = 40,
                imageUrl = "https://static.garmincdn.com/pumac/forerunner-955/forerunner-955-01.png",
                description = "GPS running watch với đầy đủ tính năng."
            ),
            ProductEntity(
                id = "26",
                name = "SteelSeries Apex Pro", brand = "SteelSeries", category = "Bàn phím",
                origin = "Denmark", price = 199.0, stock = 60,
                imageUrl = "https://cdn.steelseries.com/images/products/apex-pro-tkl/apex-pro-tkl-main.png",
                description = "Mechanical keyboard với adjustable switches."
            ),
            ProductEntity(
                id = "27",
                name = "DJI Mini 3 Pro", brand = "DJI", category = "Máy ảnh",
                origin = "China", price = 759.0, stock = 35,
                imageUrl = "https://www.dji.com/vn/mini-3-pro/images/dji-mini-3-pro-drone-1.png",
                description = "Compact drone với 4K video và obstacle avoidance."
            ),
            ProductEntity(
                id = "28",
                name = "PlayStation 5", brand = "Sony", category = "Gaming",
                origin = "Japan", price = 499.0, stock = 25,
                imageUrl = "https://gmedia.playstation.com/is/image/SIEPDC/ps5-product-thumbnail-01-en-14sep21",
                description = "Next-gen gaming console."
            ),
            ProductEntity(
                id = "29",
                name = "Xbox Series X", brand = "Microsoft", category = "Gaming",
                origin = "USA", price = 499.0, stock = 30,
                imageUrl = "https://compass-ssl.xbox.com/assets/83/53/83534a32-5cae-47d7-adbc-c2b8c8b1581c.png",
                description = "Most powerful Xbox ever."
            ),
            ProductEntity(
                id = "30",
                name = "Samsung Galaxy Tab S9", brand = "Samsung", category = "Tablet",
                origin = "Korea", price = 799.0, stock = 40,
                imageUrl = "https://images.samsung.com/is/image/samsung/p6pim/vn/2307/gallery/vn-galaxy-tab-s9-ultra-sm-x916bzadexx-thumb-534856135",
                description = "Premium Android tablet với S Pen."
            ),
            ProductEntity(
                id = "31",
                name = "LG C3 OLED TV 55", brand = "LG", category = "Màn hình",
                origin = "Korea", price = 1299.0, stock = 15,
                imageUrl = "https://www.lg.com/us/images/tvs/md07500396/gallery/desktop-01.jpg",
                description = "55-inch OLED TV với 4K HDR."
            ),
            ProductEntity(
                id = "32",
                name = "Corsair K70 RGB TKL", brand = "Corsair", category = "Bàn phím",
                origin = "USA", price = 169.0, stock = 55,
                imageUrl = "https://www.corsair.com/medias/sys_master/images/images/hd5/hd0/9118848368670/-CH-9119014-NA-Gallery-K70-RGB-TKL-01.png",
                description = "Tenkeyless mechanical gaming keyboard."
            ),
            ProductEntity(
                id = "33",
                name = "HyperX Cloud Alpha", brand = "HyperX", category = "Tai nghe",
                origin = "USA", price = 99.0, stock = 70,
                imageUrl = "https://www.hyperx.com/media-library/images/products/cloud-alpha-wireless/cloud-alpha-wireless-main.png",
                description = "Gaming headset với dual chamber drivers."
            ),
            ProductEntity(
                id = "34",
                name = "Fitbit Charge 5", brand = "Fitbit", category = "Đồng hồ",
                origin = "USA", price = 179.0, stock = 90,
                imageUrl = "https://www.fitbit.com/global/content/dam/fitbit/global/pdp/devices/charge-5/charge-5-hero-black.png",
                description = "Advanced fitness tracker với ECG."
            ),
            ProductEntity(
                id = "35",
                name = "SanDisk Extreme Pro 1TB", brand = "SanDisk", category = "Phụ kiện",
                origin = "USA", price = 129.0, stock = 100,
                imageUrl = "https://www.westerndigital.com/content/dam/store/en-us/assets/products/memory-cards/sandisk-extreme-pro-sdxc-uhs-i/product/tab/sd-card-extreme-pro-uhs-i-128gb-front.png",
                description = "High-speed SD card 1TB cho camera."
            ),
            ProductEntity(
                id = "36",
                name = "Belkin BoostCharge Pro", brand = "Belkin", category = "Phụ kiện",
                origin = "USA", price = 79.0, stock = 85,
                imageUrl = "https://www.belkin.com/dw/image/v2/BDWJ_PRD/on/demandware.static/-/Sites-master-catalog/default/dw8a8c8e0e/images/large/B2B040-BLK_01.png",
                description = "Wireless charging pad 15W."
            ),
            ProductEntity(
                id = "37",
                name = "Oculus Quest 3", brand = "Meta", category = "Gaming",
                origin = "USA", price = 499.0, stock = 35,
                imageUrl = "https://www.meta.com/quest/products/quest-3/",
                description = "Mixed reality VR headset."
            ),
            ProductEntity(
                id = "38",
                name = "Canon RF 24-70mm f/2.8", brand = "Canon", category = "Máy ảnh",
                origin = "Japan", price = 2299.0, stock = 18,
                imageUrl = "https://www.canon-europe.com/media/rf_24-70mm_f2-8l_is_usm_tcm14-1772422.png",
                description = "Professional zoom lens cho mirrorless."
            ),
            ProductEntity(
                id = "39",
                name = "Sony WF-1000XM5", brand = "Sony", category = "Tai nghe",
                origin = "Japan", price = 299.0, stock = 65,
                imageUrl = "https://www.sony.com/image/4c8b8b8b8b8b8b8b8b8b8b8b8b8b8b",
                description = "True wireless earbuds với noise canceling."
            ),
            ProductEntity(
                id = "40",
                name = "Logitech G Pro X Superlight", brand = "Logitech", category = "Phụ kiện",
                origin = "Switzerland", price = 149.0, stock = 75,
                imageUrl = "https://resource.logitechg.com/w_800,c_lpad,ar_1:1,q_auto,f_auto,dpr_1.0/d_transparent.gif/content/dam/logitechg/products/mice/g-pro-x-superlight-2/gallery/g-pro-x-superlight-2-mouse-top-view-graphite.png",
                description = "Ultra-lightweight wireless gaming mouse."
            )
        )
    }
}
