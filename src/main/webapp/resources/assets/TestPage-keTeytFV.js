import{d as u,c as o,b,a as e,F as s,r as f,u as i,o as a,n as c,t as d}from"./index-Cr3oa366.js";const P="/assets/lollipop-CZ1jDQzx.jpg",y=`@theme {
  --color-kb-yellow-positive: #ffbc00;
  --color-kb-yellow: #ffd338;
  --color-kb-yellow-native: #ffcc00;
  --color-kb-gray-dark: #60584c;
  --color-kb-gray-light: #918274;
  --color-kb-ui-01: #000000;
  --color-kb-ui-02: #26282c;
  --color-kb-ui-03: #484b51;
  --color-kb-ui-04: #696e76;
  --color-kb-ui-05: #8c949e;
  --color-kb-ui-06: #aab0b8;
  --color-kb-ui-07: #c6cbd0;
  --color-kb-ui-08: #dde1e4;
  --color-kb-ui-09: #ebeef0;
  --color-kb-ui-10: #f7f7f8;
  --color-kb-ui-11: #ffffff;
  --color-positive: #287eff;
  --color-error: #f23f3f;
  --color-error-input: #ff5858;
  --color-caution: #ffd338;
}
`,x=`/* Pretendard */
@font-face {
  font-family: "Pretendard-Thin";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-Thin.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-ExtraLight";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-ExtraLight.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-Light";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-Light.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-Medium";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-Medium.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-Regular";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-Regular.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-SemiBold";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-SemiBold.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-Bold";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-Bold.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-ExtraBold";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-ExtraBold.otf") format("opentype");
}
@font-face {
  font-family: "Pretendard-Black";
  src: url("@/assets/fonts/Pretendard-SemiBold/Pretendard-Black.otf") format("opentype");
}

@theme {
  --font-pretendard-thin: "Pretendard-Thin";
  --font-pretendard-extralight: "Pretendard-ExtraLight";
  --font-pretendard-light: "Pretendard-Light";
  --font-pretendard-medium: "Pretendard-Medium";
  --font-pretendard-regular: "Pretendard-Regular";
  --font-pretendard-semibold: "Pretendard-SemiBold";
  --font-pretendard-bold: "Pretendard-Bold";
  --font-pretendard-extrabold: "Pretendard-ExtraBold";
  --font-pretendard-black: "Pretendard-Black";
}
`,h={class:"flex flex-col"},g={class:"border border-gray-300 mt-10"},k={class:"flex flex-col items-center"},B={class:"text-center"},v={class:"text-center"},_={class:"border border-gray-300 mt-10"},S={class:"text-center"},E=u({__name:"TestPage",setup(L){const m=y.split(`
`).map(n=>n.trim()).filter(n=>n.startsWith("--color-")).map(n=>{const[r,t]=n.replace(";","").split(":").map(l=>l.trim());return{name:r.replace("--color-",""),value:t}}),p=x.split(`
`).map(n=>n.trim()).filter(n=>n.startsWith("--font-")).map(n=>{const[r,t]=n.replace(";","").split(":").map(l=>l.trim());return{name:r.replace("--font-",""),value:t}});return(n,r)=>(a(),o("div",null,[r[2]||(r[2]=b('<h1 class="text-center text-3xl font-bold underline text-red-300 mb-12">Testing Tailwind</h1><br><div class="flex flex-col items-center gap-6 p-7 md:flex-row md:gap-8 rounded-2xl"><div><img class="size-48 shadow-xl rounded-md" alt="사진" src="'+P+'"></div><div class="flex flex-col items-center gap-1"><span class="text-2xl font-pretendard-thin">Pretendard Thin 글꼴 적용</span><span class="font-medium text-kb-yellow-positive">커스텀 KB 색상 적용</span><span class="flex gap-2 font-medium text-kb-gray-dark"><span>No 4</span><span>·</span><span>2025</span></span></div></div><br>',4)),e("div",h,[e("table",g,[r[0]||(r[0]=e("thead",null,[e("tr",null,[e("th",null,"색상"),e("th",null,"색상값"),e("th",null,"클래스명 예시")])],-1)),e("tbody",null,[(a(!0),o(s,null,f(i(m),t=>(a(),o("tr",{key:t.name},[e("td",k,[e("div",{class:"w-7 h-7 rounded-full",style:c({backgroundColor:t.value})},null,4)]),e("td",B,d(t.value),1),e("td",v,"--bg-"+d(t.name),1)]))),128))])]),e("table",_,[r[1]||(r[1]=e("thead",null,[e("tr",null,[e("th",null,"폰트"),e("th",null,"클래스명")])],-1)),e("tbody",null,[(a(!0),o(s,null,f(i(p),t=>(a(),o("tr",{key:t.name},[e("td",{style:c({fontFamily:t.value})},d(t.value),5),e("td",S,"--font-"+d(t.name),1)]))),128))])])])]))}});export{E as default};
