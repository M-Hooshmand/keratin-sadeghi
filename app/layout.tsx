import type { Metadata } from "next";
import "@fontsource-variable/vazirmatn";
import "./globals.css";
import Sidebar from "@/components/Sidebar";

export const metadata: Metadata = {
  title: "سالن کراتین خانم صادقی",
  description: "مدیریت مشتریان، نوبت‌ها، مواد مصرفی و حسابداری سالن",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="fa" dir="rtl">
      <body>
        <div className="relative min-h-screen">
          <div
            aria-hidden
            className="pointer-events-none fixed inset-0 z-0 overflow-hidden"
          >
            <span className="absolute -left-24 top-24 h-72 w-72 rounded-full bg-[#ff8fc4]/20 blur-3xl" />
            <span className="absolute -right-20 bottom-16 h-80 w-80 rounded-full bg-[#e6c2ff]/30 blur-3xl" />
            <span className="twinkle absolute left-[18%] top-[30%] text-2xl text-hot/25">
              ✦
            </span>
            <span className="twinkle absolute bottom-[18%] left-[42%] text-lg text-bubble/40 [animation-delay:1.6s]">
              ✦
            </span>
          </div>
          <Sidebar />
          <div className="relative z-10 lg:mr-64">
            <main className="mx-auto max-w-6xl px-4 py-6 md:px-8 md:py-10">
              {children}
            </main>
          </div>
        </div>
      </body>
    </html>
  );
}
