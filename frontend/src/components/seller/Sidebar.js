import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import styles from "../../styles/seller/Sidebar.module.css";

/**
 * 공용 Sidebar (본문 자동 밀기 포함)
 *
 * props
 * - base: 링크 prefix (예: `/seller/mart/1`)
 * - width: 열림 시 너비(px). 기본 180 (수정됨)
 * - breakpoint: 이 값 이하에선 자동 닫힘/본문 안 밀기. 기본 768
 * - pushContent: true면 데스크탑에서 body에 padding-left를 넣어 본문을 오른쪽으로 밀어줌. 기본 true
 */
export default function Sidebar({
  base = "/seller/mart/1",
  width = 200,
  breakpoint = 768,
  pushContent = true,
}) {
  // 초기 상태: 데스크탑이면 열림, 모바일이면 닫힘
  const [isOpen, setIsOpen] = useState(() => {
    if (typeof window === "undefined") return true;
    return window.innerWidth > breakpoint;
  });

  // 창 크기 변경 시 자동 동기화
  useEffect(() => {
    const onResize = () => {
      setIsOpen(window.innerWidth > breakpoint);
    };
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, [breakpoint]);

  // 모든 페이지 공통: 사이드바 열림/닫힘에 따라 본문을 자동으로 오른쪽으로 밀기
  // - 데스크탑(> breakpoint)에서만 적용
  // - 오버레이(겹치기)로 쓰고 싶으면 pushContent=false로 끄기
  useEffect(() => {
    if (!pushContent || typeof document === "undefined") return;
    const isDesktop = window.innerWidth > breakpoint;
    document.body.style.transition = "padding-left 200ms ease";
    document.body.style.paddingLeft =
      isDesktop && isOpen ? `${width}px` : "0px";
    return () => {
      document.body.style.paddingLeft = "";
      document.body.style.transition = "";
    };
  }, [isOpen, pushContent, width, breakpoint]);

  // 토글 버튼의 left 위치
  const leftPx = isOpen ? `${width}px` : "0px";

  return (
    <>
      <aside
        // className을 styles 객체에서 가져오도록 수정
        className={`${styles.sidebar} ${!isOpen ? styles.sidebarClosed : ""}`}
        style={{ width: `${width}px` }}
      >
        <div>
          <h3>마트 관리</h3>
          <ul>
            <li>
              <Link to={`${base}`}>마트 대시보드</Link>
            </li>
            <li>
              <Link to={`${base}/info`}>나의 마트</Link>
            </li>
            <li>
              <Link to={`${base}/check`}>등록 상품 확인</Link>
            </li>
            <li>
              <Link to={`${base}/register`}>상품 등록</Link>
            </li>
            <li>
              <Link to={`${base}/customers`}>주문 목록 확인</Link>
            </li>
          </ul>
        </div>
        <div className={styles.sidebarFooter}>
          <hr />
          <Link to="/seller/dashboard">전체 대시보드로 돌아가기</Link>
        </div>
      </aside>

      {/* 삼각형 토글 버튼 (◀/▶) */}
      <button
        className={styles.sidebarToggle}
        onClick={() => setIsOpen((v) => !v)}
        style={{ left: leftPx }}
        aria-label="사이드바 토글"
        aria-pressed={isOpen}
      >
        <span
          className={`${styles.triangle} ${isOpen ? styles.open : styles.close}`}
        />
      </button>
    </>
  );
}