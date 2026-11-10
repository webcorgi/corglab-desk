import React,{useState} from 'react';
import{createRoot}from'react-dom/client';
import './style.css';
function App(){
 const[page,setPage]=useState('inquiry'),[form,setForm]=useState({name:'',email:'',company:'',message:''});
 const[msg,setMsg]=useState(''),[username,setUsername]=useState(''),[password,setPassword]=useState(''),[items,setItems]=useState([]),[filter,setFilter]=useState('');
 const statuses={new:'신규',in_progress:'처리 중',done:'완료'};
 function auth(){return {'Authorization':'Basic '+btoa(Array.from(new TextEncoder().encode(username+':'+password),x=>String.fromCharCode(x)).join(''))};}
 async function send(e){e.preventDefault();setMsg('접수 중...');try{
  const r=await fetch('/api/inquiries',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form)});
  if(!r.ok)throw Error('접수 실패');setMsg('문의가 접수됐습니다.');setForm({name:'',email:'',company:'',message:''});
 }catch{setMsg('접수에 실패했습니다.');}}
 async function load(){try{
  const r=await fetch('/api/admin/inquiries'+(filter?'?status='+filter:''),{headers:auth()});
  if(!r.ok)throw Error('인증 실패 또는 서버 오류');setItems(await r.json());setMsg('');
 }catch(e){setMsg(e.message);}}
 async function change(id,status){
  const r=await fetch('/api/admin/inquiries/'+id,{method:'PATCH',headers:{...auth(),'Content-Type':'application/json'},body:JSON.stringify({status})});
  if(r.ok)load();else setMsg('상태 변경 실패');
 }
 return <div className="app"><header><strong>CORGLAB <span>Desk</span></strong><nav><button onClick={()=>setPage('inquiry')}>문의 접수</button><button onClick={()=>setPage('admin')}>관리자</button></nav></header><main>{page==='inquiry'?<section className="card"><h1>프로젝트 문의</h1><p>상담 내용을 남기시면 확인 후 연락드리겠습니다.</p><form onSubmit={send}><label>이름<input required maxLength="100" value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>이메일<input required type="email" maxLength="200" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>회사명<input maxLength="150" value={form.company} onChange={e=>setForm({...form,company:e.target.value})}/></label><label>문의 내용<textarea required maxLength="4000" rows="6" value={form.message} onChange={e=>setForm({...form,message:e.target.value})}/></label><button>접수하기</button></form></section>:<section className="card"><h1>문의 관리</h1><p>로컬 개발용 관리자 로그인. 공용 PC에서 사용하지 마세요.</p><div className="controls"><input placeholder="아이디" autoComplete="username" value={username} onChange={e=>setUsername(e.target.value)}/><input type="password" autoComplete="current-password" placeholder="비밀번호" value={password} onChange={e=>setPassword(e.target.value)}/><select value={filter} onChange={e=>setFilter(e.target.value)}><option value="">전체</option>{Object.entries(statuses).map(([k,v])=><option key={k} value={k}>{v}</option>)}</select><button onClick={load}>조회</button></div>{items.map(x=><article key={x.id}><b>{x.name} · {x.company||'개인'}</b><small>{x.email} · {x.category}</small><p>{x.message}</p>{x.summary&&<blockquote>AI 요약: {x.summary}</blockquote>}<select value={x.status} onChange={e=>change(x.id,e.target.value)}>{Object.entries(statuses).map(([k,v])=><option key={k} value={k}>{v}</option>)}</select></article>)}</section>}{msg&&<p role="status">{msg}</p>}</main><footer>© CORGLAB Desk · MVP</footer></div>;
}
createRoot(document.getElementById('root')).render(<App/>);
