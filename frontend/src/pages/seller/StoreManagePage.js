import React, {useState, useEffect, useMemo, useRef} from 'react';
import {useParams, useNavigate} from 'react-router-dom';
import axios from 'axios';
import styles from '../../styles/seller/StoreManagePage.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';

// 👇 [전역 변수] 한 번만 선언해서 계속 씁니다.
const BASE_URL = process.env.REACT_APP_API_URL || '';

const EditableField = ({label, children, onSave}) => {
  const [isEditing, setIsEditing] = useState(false);

  const handleSaveClick = async () => {
    try {
      await onSave();
      setIsEditing(false);
      alert('성공적으로 저장되었습니다.');
    } catch (error) {
      console.error('Update failed:', error);
      alert(`저장에 실패했습니다: ${error.response?.data?.message || error.message}`);
    }
  };

  return (
    <div className={styles.fieldContainer}>
      <label>{label}</label>
      <div className={styles.inputGroup}>
        {children(isEditing)}
        <button
          onClick={() => isEditing ? handleSaveClick() : setIsEditing(true)}
          className={styles.editButton}
        >
          {isEditing ? '저장' : '수정'}
        </button>
      </div>
    </div>
  );
};

// 매장 정보 탭
const StoreInfoTab = ({storeId}) => {
  const [storeData, setStoreData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [postcode, setPostcode] = useState('');
  const [baseAddress, setBaseAddress] = useState('');
  const [detailAddress, setDetailAddress] = useState('');
  const detailAddressRef = useRef(null);

  useEffect(() => {
    const fetchStoreData = async () => {
      try {
        setLoading(true);
        // 👇 [수정] BASE_URL 추가
        const response = await axios.get(`${BASE_URL}/api/seller/store/${storeId}`);
        setStoreData(response.data);

        const fullAddress = response.data.address || '';
        const lastCommaIndex = fullAddress.lastIndexOf(',');
        if (lastCommaIndex > -1) {
          setBaseAddress(fullAddress.substring(0, lastCommaIndex).trim());
          setDetailAddress(fullAddress.substring(lastCommaIndex + 1).trim());
        } else {
          setBaseAddress(fullAddress);
          setDetailAddress('');
        }

      } catch (err) {
        console.error("Failed to fetch store data:", err);
        setError('매장 정보를 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchStoreData();
  }, [storeId]);

  useEffect(() => {
    const script = document.createElement('script');
    script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
    script.async = true;
    document.head.appendChild(script);
    return () => {
      document.head.removeChild(script);
    };
  }, []);

  const handleOpenPostcode = () => {
    if (window.daum && window.daum.Postcode) {
      new window.daum.Postcode({
        oncomplete: (data) => {
          setPostcode(data.zonecode);
          setBaseAddress(data.address);
          detailAddressRef.current?.focus();
        },
      }).open();
    }
  };

  const handleChange = (field, value) => {
    setStoreData(prev => ({...prev, [field]: value}));
  };

  const handleSave = async (endpoint, payload) => {
    // 👇 [수정] BASE_URL 추가
    return axios.patch(`${BASE_URL}/api/seller/store/${storeId}/${endpoint}`, payload);
  };

  const handleSaveName = () => handleSave('name', {name: storeData.name});
  const handleSavePhoneNumber = () => handleSave('phone-number', {phoneNumber: storeData.phoneNumber});
  const handleSaveHours = () => handleSave('hours', {
    openingTime: storeData.openingTime,
    closingTime: storeData.closingTime
  });
  const handleSavePaymentMethod = () => handleSave('payment-method', {paymentMethod: storeData.paymentMethod});

  // 👇 [대폭 수정] 옛날 SGIS 토큰 로직 다 지우고, 백엔드에 요청하는 깔끔한 함수로 변경!
  const geocodeWGS84 = async (addr) => {
    try {
      // 백엔드 주소로 바로 요청 (토큰 필요 없음)
      const url = `${BASE_URL}/api/sgis/geocode?address=${encodeURIComponent(addr)}`;

      const res = await fetch(url);
      const data = await res.json();

      if (data.errCd && data.errCd !== 0) {
        console.warn('[SGIS Error]', data.errMsg);
        return null;
      }
      const firstResult = data?.result?.resultdata?.[0];
      return firstResult ? { lat: String(firstResult.y), lng: String(firstResult.x) } : null;

    } catch (error) {
      console.error("Geocoding failed", error);
      throw error;
    }
  };
  // 👆 여기까지 수정 끝 (getSgisToken 함수 등은 이제 필요 없어서 삭제됨)

  const handleSaveAddress = async () => {
    const fullAddress = `${baseAddress}, ${detailAddress}`;
    if (!baseAddress || !detailAddress) {
      alert('주소와 상세주소를 모두 입력해주세요.');
      throw new Error("주소가 불완전합니다.");
    }
    const coords = await geocodeWGS84(fullAddress);
    if (!coords) {
      alert("유효하지 않은 주소입니다. 주소를 다시 확인해주세요.");
      throw new Error("주소를 좌표로 변환할 수 없습니다.");
    }
    const payload = {
      address: fullAddress,
      latitude: coords.lat,
      longitude: coords.lng
    };
    await handleSave('address', payload);
    setStoreData(prev => ({...prev, address: fullAddress}));
  };

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;
  if (!storeData) return <div>매장 데이터가 없습니다.</div>;

  return (
    <div className={styles.form}>
      <EditableField label="매장명" onSave={handleSaveName}>
        {(isEditing) => <input type="text" value={storeData.name} onChange={(e) => handleChange('name', e.target.value)}
                               disabled={!isEditing}/>}
      </EditableField>

      <EditableField label="주소" onSave={handleSaveAddress}>
        {(isEditing) => (
          <div className={styles.addressGroup}>
            <div className={styles.postcodeGroup}>
              <input type="text" value={postcode} placeholder="우편번호" readOnly/>
              <button type="button" onClick={handleOpenPostcode} disabled={!isEditing}
                      className={styles.smallButton}>우편번호 찾기
              </button>
            </div>
            <input type="text" value={baseAddress} placeholder="주소" readOnly/>
            <input ref={detailAddressRef} type="text" value={detailAddress}
                   onChange={(e) => setDetailAddress(e.target.value)} placeholder="상세주소" disabled={!isEditing}/>
          </div>
        )}
      </EditableField>

      <EditableField label="전화번호" onSave={handleSavePhoneNumber}>
        {(isEditing) => <input type="tel" value={storeData.phoneNumber}
                               onChange={(e) => handleChange('phoneNumber', e.target.value.replace(/[^0-9-]/g, ''))}
                               disabled={!isEditing}/>}
      </EditableField>

      <EditableField label="영업 시간" onSave={handleSaveHours}>
        {(isEditing) => (
          <div className={styles.timeGroup}>
            <input type="time" value={storeData.openingTime}
                   onChange={(e) => handleChange('openingTime', e.target.value)} disabled={!isEditing}/>
            <span>~</span>
            <input type="time" value={storeData.closingTime}
                   onChange={(e) => handleChange('closingTime', e.target.value)} disabled={!isEditing}/>
          </div>
        )}
      </EditableField>

      <EditableField label="결제 방식" onSave={handleSavePaymentMethod}>
        {(isEditing) => (
          <select value={storeData.paymentMethod}
                  onChange={(e) => handleChange('paymentMethod', Number(e.target.value))} disabled={!isEditing}>
            <option value={0}>현장 결제</option>
            <option value={1}>현장 + 카드 결제</option>
          </select>
        )}
      </EditableField>

      <div className={styles.fieldContainer}>
        <label>사업자 등록번호</label>
        <div className={styles.inputGroup}>
          <input type="text" value={storeData.businessNumber} readOnly className={styles.readOnlyInput}/>
        </div>
      </div>
    </div>
  );
};

// 직원 관리 탭
const EmployeeListTab = ({storeId, refreshEmployees}) => {
  const [employees, setEmployees] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [checkedState, setCheckedState] = useState({});
  const [isDelegateModalOpen, setIsDelegateModalOpen] = useState(false);

  const fetchEmployees = async () => {
    try {
      // 👇 [수정] BASE_URL 추가
      const response = await axios.get(`${BASE_URL}/api/seller/store/${storeId}/employees`);
      const sortedEmployees = response.data.sort((a, b) => {
        if (a.position === '총괄 관리자') return -1;
        if (b.position === '총괄 관리자') return 1;
        return 0;
      });
      setEmployees(sortedEmployees);
      setCheckedState({});
    } catch (error) {
      console.error('Failed to fetch employees:', error);
      alert('직원 목록을 불러오는 데 실패했습니다.');
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, [storeId, refreshEmployees]);

  const filteredEmployees = useMemo(() =>
    employees.filter(emp =>
      emp.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      emp.email.toLowerCase().includes(searchTerm.toLowerCase())
    ), [employees, searchTerm]);

  const handleToggleCheck = (userId) => {
    setCheckedState(prev => ({...prev, [userId]: !prev[userId]}));
  };

  const handleToggleAll = (e) => {
    const newCheckedState = {};
    if (e.target.checked) {
      filteredEmployees.forEach(emp => {
        newCheckedState[emp.userId] = true;
      });
    }
    setCheckedState(newCheckedState);
  };

  const selectedIds = Object.keys(checkedState).filter(id => checkedState[id]).map(Number);
  const selectedCount = selectedIds.length;
  const isAllChecked = filteredEmployees.length > 0 && selectedCount === filteredEmployees.length;

  const handleDelegateManager = async () => {
    if (selectedCount !== 1) {
      alert('총괄 관리자를 위임할 직원 1명만 선택해주세요.');
      return;
    }
    const selectedEmployee = employees.find(emp => emp.userId === selectedIds[0]);
    if (selectedEmployee.position === '총괄 관리자') {
      alert('이미 총괄 관리자인 직원입니다.');
      return;
    }

    setIsDelegateModalOpen(true);
  };

  const confirmDelegateManager = async () => {
    try {
      // 👇 [수정] BASE_URL 추가
      await axios.post(`${BASE_URL}/api/seller/store/${storeId}/employees/delegate`, {newManagerId: selectedIds[0]});
      alert('총괄 관리자가 위임되었습니다.');
      fetchEmployees();
    } catch (error) {
      console.error('Failed to delegate manager:', error);
      alert('총괄 관리자 위임에 실패했습니다.');
    } finally {
      setIsDelegateModalOpen(false);
    }
  };

  const handleEmployeeDelete = async () => {
    if (selectedCount === 0) {
      alert('삭제할 직원을 선택해주세요.');
      return;
    }
    const managerIsSelected = employees.some(emp =>
      selectedIds.includes(emp.userId) && emp.position === '총괄 관리자'
    );

    if (managerIsSelected) {
      alert('총괄 관리자는 삭제할 수 없습니다.');
      return;
    }

    if (window.confirm(`${selectedCount}명의 직원을 정말 삭제하시겠습니까?`)) {
      try {
        // 👇 [수정] BASE_URL 추가
        await axios.post(`${BASE_URL}/api/seller/store/${storeId}/employees/delete`, {userIds: selectedIds});
        alert('삭제되었습니다.');
        fetchEmployees();
      } catch (error) {
        console.error('Failed to delete employees:', error);
        alert('직원 삭제에 실패했습니다.');
      }
    }
  };

  return (
    <div className={styles.employeeSubTabContainer}>
      <div className={styles.listHeader}>
        <input type="text" placeholder="이름 또는 이메일로 검색" className={styles.searchBar} value={searchTerm}
               onChange={e => setSearchTerm(e.target.value)}/>
      </div>
      <div className={styles.tableWrapper}>
        <table className={`${styles.employeeTable} ${styles.employeeListTable}`}>
          <colgroup>
            <col/>
            <col/>
            <col/>
            <col/>
          </colgroup>
          <thead>
          <tr>
            <th><input type="checkbox" onChange={handleToggleAll} checked={isAllChecked}/></th>
            <th>이름</th>
            <th>이메일</th>
            <th>직위</th>
          </tr>
          </thead>
          <tbody>
          {filteredEmployees.map(emp => (
            <tr key={emp.userId}>
              <td><input type="checkbox" checked={!!checkedState[emp.userId]}
                         onChange={() => handleToggleCheck(emp.userId)}/></td>
              <td>{emp.name}</td>
              <td>{emp.email}</td>
              <td>{emp.position}</td>
            </tr>
          ))}
          </tbody>
        </table>
      </div>
      <div className={styles.actionButtons}>
        <button onClick={handleEmployeeDelete} className={styles.deleteButton}>직원 삭제</button>
      </div>

      {isDelegateModalOpen && (
        <div className={styles.modalBackdrop}>
          <div className={styles.modalContent}>
            <h4>총괄 관리자 위임 확인</h4>
            <p>정말로 총괄 관리자 권한을 위임하시겠습니까?<br/>이 작업은 되돌릴 수 없습니다.</p>
            <div className={styles.modalActions}>
              <button className={styles.cancelButton} onClick={() => setIsDelegateModalOpen(false)}>취소</button>
              <button className={styles.addButton} onClick={confirmDelegateManager}>확인</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const AddEmployeeTab = ({storeId, onEmployeeAdded}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [emailInput, setEmailInput] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [stagedEmployees, setStagedEmployees] = useState([]);
  const [isSearching, setIsSearching] = useState(false);

  const handleCheckEmail = async () => {
    if (!emailInput) return;
    setIsSearching(true);
    setSearchResult(null);
    try {
      // 👇 [수정] BASE_URL 추가
      const response = await axios.get(`${BASE_URL}/api/seller/store/${storeId}/employees/search-user`, {
        params: {email: emailInput}
      });
      const {userId, name, email, status} = response.data;
      if (status === "OK") {
        setSearchResult({status: 'found', user: {userId, name, email}});
      } else if (status === "ALREADY_EMPLOYEE") {
        setSearchResult({status: 'already_employee', message: '이미 등록된 직원입니다.'});
      } else {
        setSearchResult({status: 'not_found', message: '가입되지 않았거나 유효하지 않은 이메일입니다.'});
      }
    } catch (error) {
      console.error('Failed to search user:', error);
      setSearchResult({status: 'error', message: '검색 중 오류가 발생했습니다.'});
    } finally {
      setIsSearching(false);
    }
  };

  const handleAddStagedEmployee = () => {
    if (searchResult?.status === 'found') {
      if (stagedEmployees.some(emp => emp.email === searchResult.user.email)) {
        alert('이미 추가 목록에 있는 직원입니다.');
        return;
      }
      setStagedEmployees([...stagedEmployees, {...searchResult.user, checked: true}]);
      closeModal();
    }
  };

  const handleToggleCheck = (userId) => {
    setStagedEmployees(stagedEmployees.map(emp => emp.userId === userId ? {...emp, checked: !emp.checked} : emp));
  };

  const handleToggleAllAdd = (e) => {
    setStagedEmployees(stagedEmployees.map(emp => ({...emp, checked: e.target.checked})));
  };

  const handleRemoveStagedEmployee = (userId) => {
    setStagedEmployees(stagedEmployees.filter(emp => emp.userId !== userId));
  };

  const handleApplyChanges = async () => {
    const selectedIds = stagedEmployees.filter(emp => emp.checked).map(emp => emp.userId);
    if (selectedIds.length === 0) {
      alert('추가할 직원을 선택해주세요.');
      return;
    }
    try {
      // 👇 [수정] BASE_URL 추가
      await axios.post(`${BASE_URL}/api/seller/store/${storeId}/employees/add`, {userIds: selectedIds});
      alert(`${selectedIds.length}명의 직원이 성공적으로 추가되었습니다.`);
      setStagedEmployees([]);
      onEmployeeAdded();
    } catch (error) {
      console.error('Failed to add employees:', error);
      alert('직원 추가에 실패했습니다.');
    }
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEmailInput('');
    setSearchResult(null);
  };

  const isAllStagedChecked = stagedEmployees.length > 0 && stagedEmployees.every(e => e.checked);

  return (
    <div className={styles.employeeSubTabContainer}>
      <div className={styles.tableHeader}>
        <h3>직원 추가 목록</h3>
        <button className={styles.editButton} onClick={() => setIsModalOpen(true)}>+ 직원 추가</button>
      </div>
      <div className={styles.tableWrapper}>
        <table className={`${styles.employeeTable} ${styles.addEmployeeTable}`}>
          <colgroup>
            <col/>
            <col/>
            <col/>
            <col/>
            <col/>
          </colgroup>
          <thead>
          <tr>
            <th><input type="checkbox" checked={isAllStagedChecked} onChange={handleToggleAllAdd}/></th>
            <th>이름</th>
            <th>이메일</th>
            <th>직위</th>
            <th></th>
          </tr>
          </thead>
          <tbody>
          {stagedEmployees.map(emp => (
            <tr key={emp.userId}>
              <td><input type="checkbox" checked={emp.checked} onChange={() => handleToggleCheck(emp.userId)}/></td>
              <td>{emp.name}</td>
              <td>{emp.email}</td>
              <td>직원</td>
              <td>
                <button className={styles.removeButton} onClick={() => handleRemoveStagedEmployee(emp.userId)}>-
                </button>
              </td>
            </tr>
          ))}
          </tbody>
        </table>
      </div>
      <button className={styles.applyButton} onClick={handleApplyChanges}>적용하기</button>

      {isModalOpen && (
        <div className={styles.modalBackdrop} onClick={closeModal}>
          <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
            <h4>직원 이메일로 추가</h4>
            <div className={styles.inputGroup}>
              <input type="email" placeholder="직원의 이메일을 입력하세요" value={emailInput}
                     onChange={e => setEmailInput(e.target.value)} disabled={isSearching}/>
              <button className={styles.smallButton} onClick={handleCheckEmail} disabled={isSearching}>
                {isSearching ? '확인 중...' : '확인하기'}
              </button>
            </div>
            {searchResult?.status === 'found' &&
              <p className={styles.successText}>{searchResult.user.name}({searchResult.user.email}) 님을 추가할 수 있습니다.</p>}
            {searchResult && searchResult.status !== 'found' &&
              <p className={styles.errorText}>{searchResult.message}</p>}
            <div className={styles.modalActions}>
              <button className={styles.cancelButton} onClick={closeModal}>취소</button>
              <button className={styles.addButton} onClick={handleAddStagedEmployee}
                      disabled={searchResult?.status !== 'found'}>추가하기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const EmployeeManagementTab = ({storeId}) => {
  const [employeeActiveTab, setEmployeeActiveTab] = useState('list');
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleEmployeeAdded = () => {
    setRefreshTrigger(prev => prev + 1);
    setEmployeeActiveTab('list');
  };

  return (
    <div>
      <div className={styles.employeeNavTabs}>
        <button className={`${styles.employeeNavTab} ${employeeActiveTab === 'list' ? styles.active : ''}`}
                onClick={() => setEmployeeActiveTab('list')}>직원 목록
        </button>
        <button className={`${styles.employeeNavTab} ${employeeActiveTab === 'add' ? styles.active : ''}`}
                onClick={() => setEmployeeActiveTab('add')}>직원 추가
        </button>
      </div>
      <div className={styles.employeeTabContent}>
        {employeeActiveTab === 'list' && <EmployeeListTab storeId={storeId} refreshEmployees={refreshTrigger}/>}
        {employeeActiveTab === 'add' && <AddEmployeeTab storeId={storeId} onEmployeeAdded={handleEmployeeAdded}/>}
      </div>
    </div>
  );
};

const StoreManagePage = () => {
  const {storeId} = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('store');
  const [storeName, setStoreName] = useState('');

  useEffect(() => {
    const fetchStoreName = async () => {
      try {
        // 👇 [수정] BASE_URL 추가
        const response = await axios.get(`${BASE_URL}/api/seller/store/${storeId}`);
        setStoreName(response.data.name);
      } catch (error) {
        console.error("Failed to fetch store name", error);
      }
    };
    fetchStoreName();
  }, [storeId]);


  return (
    <>
      <Header/>
      <div className={styles.container}>
        <h2 className={styles.pageTitle}>{storeName || '매장'} 관리</h2>
        <div className={styles.navTabs}>
          <button className={`${styles.navTab} ${activeTab === 'store' ? styles.active : ''}`}
                  onClick={() => setActiveTab('store')}>매장 정보
          </button>
          <button className={`${styles.navTab} ${activeTab === 'employee' ? styles.active : ''}`}
                  onClick={() => setActiveTab('employee')}>직원 관리
          </button>
        </div>
        <div className={styles.tabContent}>
          {activeTab === 'store' && <StoreInfoTab storeId={storeId}/>}
          {activeTab === 'employee' && <EmployeeManagementTab storeId={storeId}/>}
        </div>
        <button className={styles.backButton} onClick={() => navigate('/seller/dashboard')}>대시보드로 돌아가기</button>
      </div>
      <Footer/>
    </>
  );
};

export default StoreManagePage;
