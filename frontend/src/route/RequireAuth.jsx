import React from 'react'
import { Navigate,Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthProvider'

const RequireAuth = () => {

    const {user,loading}= useAuth()


    if (loading){
        return <p>Authentication Loading ...</p>
    }
    if (!user){
        return <Navigate to="/login" replace/>
    }
  return <Outlet/>
}

export default RequireAuth